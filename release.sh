#!/bin/sh

RELEASE_LOG_FILE="pl-release.log"
RELEASE_VERSION=""

# simple function used to execute comands from the script
execute_cmd() {
    "$@" > $RELEASE_LOG_FILE
}

# check if a previous maven build was successful or not
check_build_result() {
    if tail -n 100 pl-release.log|grep -F "BUILD SUCCESS" 
    then
        return 0
    else
        return 1;
    fi
}

# check if the release version was specified
check_release_version() {
	if [ "$RELEASE_VERSION" == "" ]; then
	   echo "--version not specified. Please use: --version X"
	   exit 1
	fi
}

# clean the local repo and revert all local modifications
clean_local_repo() {
    git clean -f -d
    git reset --hard upstream/master
    rm -rf $RELEASE_LOG_FILE
}

# rollback a previous release attempt and prepare the local repo for another one
rollback() {
	check_release_version
    echo "Aborting ..."
    clean_local_repo
    git checkout develop
    clean_local_repo
    git checkout master
    clean_local_repo
    git branch -D release/$RELEASE_VERSION
    git push origin :release/$RELEASE_VERSION
    git push upstream :release/$RELEASE_VERSION
    git tag -d v$RELEASE_VERSION
    git push origin :refs/tags/v$RELEASE_VERSION
    git push upstream :refs/tags/v$RELEASE_VERSION
    echo "Done."
}

# upload the docs to jboss.org/docs
upload_docs() {
	check_release_version
    echo "Preparing documentation."
    DOCS_DIR="target/$RELEASE_VERSION"
    execute_cmd rm -rf $DOCS_DIR
    execute_cmd mkdir -p $DOCS_DIR
    execute_cmd unzip dist/target/picketlink-$RELEASE_VERSION.zip -d $DOCS_DIR/.
    execute_cmd mv $DOCS_DIR/picketlink-$RELEASE_VERSION/doc/api $DOCS_DIR/.
    execute_cmd mv $DOCS_DIR/picketlink-$RELEASE_VERSION/doc/reference $DOCS_DIR/.
    execute_cmd rm -rf $DOCS_DIR/picketlink-$RELEASE_VERSION
    execute_cmd mkdir /tmp/pl-docs
	execute_cmd sshfs picketlink@filemgmt.jboss.org:/docs_htdocs/picketlink/2 /tmp/pl-docs/
    execute_cmd cp -r $DOCS_DIR/ /tmp/pl-docs
    current_dir=$(pwd)
    cd /tmp/pl-docs
    execute_cmd unlink latest
    execute_cmd ln -s $DOCS_DIR/ latest
    cd $current_dir
    echo "Done."
}

# upload the distribution to jboss.org/downloads
upload_distribution() {
	check_release_version
	mkdir /tmp/pl-download
	sshfs picketlink@filemgmt.jboss.org:/downloads_htdocs/picketlink/2 /tmp/pl-download/
	mkdir /tmp/pl-download/$RELEASE_VERSION
	cp dist/target/picketlink-$RELEASE_VERSION.zip /tmp/pl-download/$RELEASE_VERSION
    current_dir=$(pwd)
    cd /tmp/pl-download
    execute_cmd unlink latest
    execute_cmd ln -s $RELEASE_VERSION latest
    cd $current_dir
    echo "Done."
}


# perform the release
release() {
	if [ "$DEVELOPMENT_VERSION" == "" ]; then
	   echo "--snapshot not specified. Please use: --snapshot X"
	   exit 1
	fi

	check_release_version

	rm -rf $RELEASE_LOG_FILE

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
	execute_cmd git fetch upstream master
	echo "    Pulling changes from master..."
	execute_cmd git pull upstream master
	echo "Done."
	echo ""

        read -p "Check all project dependencies before releasing ?[y/n] " FLAG_NO_DEPENDENCY_CHECK

	if [ "$FLAG_NO_DEPENDENCY_CHECK" == "y" ]; then
		echo "Checking dependencies."
		execute_cmd mvn -DskipTests=true clean install dependency:resolve -Dmaven.repo.local=/tmp/release_repo
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
	execute_cmd git checkout -b release/v$RELEASE_VERSION
	echo "Done."
	echo ""

	FLAG_PERFORM_RELEASE="false"

    echo "Releasing version $RELEASE_VERSION."
    execute_cmd perl -pi -e 's/'$DEVELOPMENT_VERSION'/'$RELEASE_VERSION'/g' `find . -name pom.xml`
    execute_cmd cd maven-plugins/picketlink-jdocbook-style/
    execute_cmd mvn -Prelease -DskipTests=true clean install
    execute_cmd cd ../../
    execute_cmd mvn -Prelease -DskipTests=true clean install
    if check_build_result; then
        echo "Done."
    else
        echo "ERROR: Release failed."
        exit 1
    fi

    execute_cmd git add .
    execute_cmd git commit -m "Release $RELEASE_VERSION."
    execute_cmd git tag -a v$RELEASE_VERSION -m "Version $RELEASE_VERSION."
    execute_cmd git push --tags upstream

	echo ""

	FLAG_PUBLISH_NEXUS="n"
	read -p "Do you want to publish artifacts to Nexus staging repository ?[y/n] " FLAG_PUBLISH_NEXUS

	if [ "$FLAG_PUBLISH_NEXUS" == "y" ]; then
		echo "Publishing artifacts to Nexus. "
		execute_cmd mvn -Prelease -DskipTests=true clean source:jar deploy
		echo "Done. You can now go to Nexus and finish release the artifacts."
	fi
	echo ""

	FLAG_UPLOAD_DOC="n"
	read -p "Do you want to upload the documentation to docs.jboss.org ?[y/n] " FLAG_UPLOAD_DOC

	if [ "$FLAG_UPLOAD_DOC" == "y" ]; then
		echo "Uploading documentation to docs.jboss.org. "
		upload_docs
		echo "Done. Check if the documentation is now available at http://docs.jboss.org/picketlink/2/latest"
	fi
	echo ""

	FLAG_UPLOAD_DIST="n"
	read -p "Do you want to upload distribution to downloads.jboss.org ?[y/n] " FLAG_UPLOAD_DIST

	if [ "$FLAG_UPLOAD_DIST" == "y" ]; then
		echo "Uploading distribution to downloads.jboss.org. "
		upload_distribution
		echo "Done. Check if the distribution is now available at http://downloads.jboss.org/picketlink/2/latest/picketlink-$RELEASE_VERSION.zip"
	fi
	echo ""

	echo "Finishing the release."
	git flow release finish $RELEASE_VERSION
        clean_local_repo
        git checkout master
        git branch -D release/v$RELEASE_VERSION
        git reset --hard upstream/master
	echo "Done."
	exit 0
}

usage()
{
cat << EOF
usage: $0 options

Use this script to release PicketLink versions.

OPTIONS:
   --snapshot      Snapshot version number to update from.
   --version       New snapshot version number to update to, if undefined, defaults to the version number updated from.
   --rollback      Undo a previous release attempt and prepares the local repo for a new one.
   --upload-docs   Only upload the docs.
EOF
}

DEVELOPMENT_VERSION=""
FLAG_NO_DEPENDENCY_CHECK="false"
ROLLBACK="false"

while true; do
  case "$1" in
	 --help)
		 usage
		 exit
		 ;;
	 --snapshot )
		 DEVELOPMENT_VERSION=$2
		 shift
		 shift
		 ;;
	 --version )
		 RELEASE_VERSION=$2
		 shift
		 shift
		 ;;
	 --rollback )
		 ROLLBACK="true"
		 shift
		 ;;
     --upload-docs )
		 upload_docs
         exit 0
         ;;
     --upload-dist )
		 upload_distribution
         exit 0
         ;;
	 -- )
	     shift;
	     break ;;
     * )
         break
         ;;
  esac
done

if [ "$ROLLBACK" == "true" ]; then
	rollback
	exit 0
fi

release