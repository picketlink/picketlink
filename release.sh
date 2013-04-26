#!/bin/sh

VERBOSE="false"

execute_cmd() {

    if [ "$VERBOSE" == "true" ]; then
        "$@"
    else
        "$@" > pl-release.log
    fi
}

check_build_result() {
    if grep -F "BUILD SUCCESS" pl-release.log 
    then
        return 0
    else
        return 1;
    fi
}

clean_local_repo() {
    git clean -f -d
    git reset --hard
    rm -rf release.properties
    rm -rf pl-release.log
}

RELEASE_VERSION=""
DEVELOPMENT_VERSION=""
FLAG_NO_DEPENDENCY_CHECK="false"
FLAG_PERFORM_RELEASE="false"

if [ "$1" == "-D"  ]; then
    shift
    VERBOSE="true"
fi
if [ "$1" == "--current-version"  ]; then
    shift
    DEVELOPMENT_VERSION="$1"
    shift
fi
if [ "$1" == "--version"  ]; then
    shift
    RELEASE_VERSION="$1"
    shift
fi
if [ "$1" == "--no-dependency-check" ]; then
    shift
    FLAG_NO_DEPENDENCY_CHECK="true"
fi

if [ "$DEVELOPMENT_VERSION" == "" ]; then
   echo "--current-version not specified. Please use: --current-version X"
   exit 1
fi
if [ "$RELEASE_VERSION" == "" ]; then
   echo "--version not specified. Please use: --version X"
   exit 1
fi

if [ "$1" == "--rollback" ]; then
    echo "Aborting ..."
    clean_local_repo
    git checkout develop
    clean_local_repo
    git checkout master
    clean_local_repo
    git branch -D release/$RELEASE_VERSION 
    echo "Done."    
    exit 0
fi

if [ "$1" == "--upload-docs" ]; then
    echo "Preparing documentation."
    DOCS_DIR="target/$RELEASE_VERSION"
    execute_cmd rm -rf $DOCS_DIR
    execute_cmd mkdir -p $DOCS_DIR
    execute_cmd unzip dist/target/picketlink-$RELEASE_VERSION.zip picketlink-$RELEASE_VERSION/doc/* -d $DOCS_DIR/.
    execute_cmd mv $DOCS_DIR/picketlink-$RELEASE_VERSION/doc/api $DOCS_DIR/.
    execute_cmd mv $DOCS_DIR/picketlink-$RELEASE_VERSION/doc/reference $DOCS_DIR/.
    rm -rf $DOCS_DIR/picketlink-$RELEASE_VERSION
    scp -i ~/.ssh/psilva.rsa -r $DOCS_DIR/ picketlink@filemgmt.jboss.org:/docs_htdocs/picketlink/3
    echo "Done."
    exit 1
fi

echo "###################################################"
echo "            PicketLink Release Script              "
echo "###################################################"

echo ""

echo "Current version: $DEVELOPMENT_VERSION"
echo "Release version: $RELEASE_VERSION"

echo ""

echo "Preparing local repository to release."
execute_cmd git checkout master
echo "    Fetching latest changes from upstream..."
execute_cmd git fetch upstream
echo "    Merging latest changes into master..."
execute_cmd git merge upstream/master
echo "    Swithing to develop branch..."
execute_cmd git checkout develop
echo "    Merging latest changes into develop branch..."
execute_cmd git merge master
echo "Done."
echo ""

if [ "$FLAG_NO_DEPENDENCY_CHECK" == "false" ]; then
    echo "Checking dependencies."
    execute_cmd mvn -DskipTests=true dependency:resolve -Dmaven.repo.local=/tmp/release_repo
    if check_build_result; then
        echo "Done."
    else
        echo "ERROR: Error checking dependencies. Check the logs."
        exit 1;
    fi
else
    echo "WARNING: Depedencies were not checked. This can impact users when downloading the project's artifacts."
fi
echo ""

echo "Preparing to release using git flow."
echo "    Starting release $RELEASE_VERSION..."
execute_cmd git flow release start $RELEASE_VERSION
echo "Done."
echo ""

echo "Preparing to release."
echo "    Executing maven-release-plugin in DryRun mode..."
execute_cmd mvn release:prepare --batch-mode -Drelease -DdevelopmentVersion=$DEVELOPMENT_VERSION -DreleaseVersion=$RELEASE_VERSION -Dtag=vRELEASE_VERSION -DdryRun -DignoreSnapshots=true -Prelease
if check_build_result; then     
     read -p "Project is ready to release. Do you want to proceed ?[y/n] " FLAG_PERFORM_RELEASE
else
     echo "ERROR: Project build failed. Can not proceed with the release. Check the logs."
     exit 1;
fi

if [ "$FLAG_PERFORM_RELEASE" == "y" ]; then
    echo "Releasing version $RELEASE_VERSION."
    execute_cmd cd maven-plugins/picketlink-jdocbook-style/
    execute_cmd perl -pi -e 's/$DEVELOPMENT_VERSION/$RELEASE_VERSION/g' `find . -name \pom.xml`
    execute_cmd mvn clean install
    execute_cmd perl -pi -e 's/$RELEASE_VERSION/$DEVELOPMENT_VERSION/g' `find . -name \pom.xml`
    execute_cmd cd ../../
    execute_cmd mvn release:prepare --batch-mode -Drelease -DdevelopmentVersion=$DEVELOPMENT_VERSION -DreleaseVersion=$RELEASE_VERSION -Dtag=v$RELEASE_VERSION -Dresume=false -Prelease -DignoreSnapshots=true
    if check_build_result; then
        echo "Done."
    else
        echo "ERROR: Release failed."
        exit 1
    fi
else
    exit 1
fi
echo ""

FLAG_PUBLISH_NEXUS="n"
read -p "Do you want to publish artifacts to Nexus stagin repository ?[y/n] " FLAG_PUBLISH_NEXUS

if [ "$FLAG_PUBLISH_NEXUS" == "y" ]; then
    echo "Publishing artifacts to Nexus. "
    execute_cmd mvn release:perform nexus:staging-close -Prelease
    echo "Done. You can now go to Nexus and finish release the artifacts."
fi
echo ""

echo "Finishing the release."
execute_cmd git flow release finish $RELEASE_VERSION
echo "Done."
