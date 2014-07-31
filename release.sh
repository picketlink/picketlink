#!/bin/sh

RELEASE_WORK_DIR="/tmp/release_work"
RELEASE_LOG_FILE="$RELEASE_WORK_DIR/pl-release.log"

prompt_for_boolean() {
    while true; do
        read -p "$1[Yy/Nn]" yn
        case $yn in
            [Yy]* ) return 0;;
            [Nn]* ) return 1;;
            * ) echo "You must provide [Yy] or [Nn].";;
        esac
    done
}

# simple function used to execute comands from the script
execute_cmd() {
    if [ ! -d "$RELEASE_WORK_DIR" ]; then
        mkdir -p "$RELEASE_WORK_DIR"
    fi

    "$@" > $RELEASE_LOG_FILE
}

# check if a previous maven build was successful or not
check_build_result() {
    if tail -n 100 $RELEASE_LOG_FILE|grep -F "BUILD SUCCESS"
    then
        return 0
    else
        return 1;
    fi
}

# check if the release version was specified
check_release_version() {
	if [ "$RELEASE_VERSION" == "" ]; then
	   echo "Release version not specified. Please use: -v RELEASE_VERSION. Eg.: -v 2.6.0.Final."
	   exit 1
	fi
}

# check if the snapshot version was specified
check_snapshot_version() {
	if [ "$SNAPSHOT_VERSION" == "" ]; then
	   echo "Current snapshot version not specified. Please use: -s CURRENT_SNAPSHOT_VERSION. Eg.: -s 2.6.0-SNAPSHOT."
	   exit 1
	fi
}

# clean the local repo and revert all local modifications
clean_working_copy() {
	echo "Fetching latest changes from upstream..."
	execute_cmd git fetch upstream master
	echo "Cleaning up working copy..."
    git clean -f -d
    git reset --hard upstream/master
	echo "    Pulling changes from master..."
	execute_cmd git pull upstream master
}

# check if all project dependencies can be resolved
check_project_dependencies() {
    if prompt_for_boolean "Check all project dependencies before releasing ?"; then
        TMP_LOCAL_MVN_REPO="$RELEASE_WORK_DIR/mvn_local_repo"

		echo "Checking project dependencies. Temporary Local Maven Repository will be created at $TMP_LOCAL_MVN_REPO"

		execute_cmd mvn -DskipTests=true clean install dependency:resolve -Dmaven.repo.local=$TMP_LOCAL_MVN_REPO

		if check_build_result; then
			echo "All project dependencies were resolved."
		else
			echo "ERROR: Error checking dependencies. Check the logs."
			exit 1;
		fi
    fi
}

# builds the project and checks if it was successful
build_project() {
    echo "Building project ..."
    execute_cmd mvn clean install
    execute_cmd mvn -DskipTests=true -Prelease clean install

    echo ""

    if check_build_result; then
        echo "Done."
    else
        echo "ERROR: Build failed."
        exit 1
    fi
}

# update project version to the release version
update_project_version() {
    echo "Updating project version to $RELEASE_VERSION..."

    execute_cmd perl -pi -e 's/'$SNAPSHOT_VERSION'/'$RELEASE_VERSION'/g' `find . -name pom.xml`

    GIT_STATUS_OUTPUT=$(git status -s)

    if [ -z "$GIT_STATUS_OUTPUT" ]; then
        echo "Project version was not updated to $RELEASE_VERSION. Make sure you provided the correct SNAPSHOT version."
        exit 1;
    fi
}

# deploy the released artifacts to jboss nexus
deploy_artifacts() {
    if prompt_for_boolean "Do you want to publish artifacts to JBoss Nexus ?"; then
		echo "Publishing artifacts to JBoss Nexus..."
		execute_cmd mvn -Prelease -DskipTests=true clean source:jar deploy
		echo "Artifacts deployed. Don't forget to Close and Release the newly created Staging Repository in JBoss Nexus."
    fi
}

# build and publish docs
publish_documentation() {
    if prompt_for_boolean "Do you want to publish documentation ?"; then
		echo "Publishing documentation..."
    fi
}

# tag the released version in github
tag_project() {
    if prompt_for_boolean "Do you want to tag this version ?"; then
        TAG_NAME = "v$RELEASE_VERSION"
        echo "Tagging version $RELEASE_VERSION..."

        execute_cmd git add .
        execute_cmd git commit -m "Release $RELEASE_VERSION."
        execute_cmd git tag -a v$RELEASE_VERSION -m "Version $RELEASE_VERSION."
        execute_cmd git push --tags upstream

        echo "Tag $TAG_NAME created for Version $RELEASE_VERSION."
    fi
}

# rollback a previous release attempt and prepare the local repo for another one
rollback() {
	check_release_version
    echo "Aborting Release: " + $RELEASE_VERSION
    clean_working_copy
    git tag -d v$RELEASE_VERSION
    git push origin :refs/tags/v$RELEASE_VERSION
    git push upstream :refs/tags/v$RELEASE_VERSION
    echo "Done."
    echo "==============================================="
    echo ""
    echo "           POST EXECUTION INSTRUCTIONS         "
    echo ""
    echo "==============================================="
    echo ""
    echo "* Go to https://repository.jboss.org/nexus/ and Drop any Staging Repository associated with the release."
    echo ""
    echo "==============================================="
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

    check_snapshot_version
	check_release_version
	clean_working_copy

	echo "==================================================="
	echo ""
	echo "            PicketLink Release Script              "
	echo ""
	echo "SNAPSHOT Version: $SNAPSHOT_VERSION"
	echo "Release Version : $RELEASE_VERSION"
	echo ""
    echo "==================================================="

    if ! prompt_for_boolean "Are sure you want to release version $RELEASE_VERSION from snapshot $SNAPSHOT_VERSION ?"; then
        echo "Aborting release."
        exit 0;
    fi

	echo ""

    update_project_version

    echo ""

    check_project_dependencies

    echo ""

    build_project

	echo ""

	tag_project

    echo ""

    deploy_artifacts

    clean_working_copy

	echo "==================================================="
	echo ""
	echo "         Version $RELEASE_VERSION Released !       "
	echo ""
    echo "==================================================="
    echo "             POST RELEASE INSTRUCTIONS             "
	echo "==================================================="
	echo ""
	echo " * Go to https://repository.jboss.org/nexus/ and Drop any Staging Repository associated with the release."
	echo " * Check if the documentation can be access at http://docs.jboss.org/picketlink/2/latest/. Make sure it is referencing the released version."
	echo " * Check if the PicketLink Installer can be accessed from http://downloads.jboss.org/picketlink/2/$RELEASE_VERSION/picketlink-installer-$RELEASE_VERSION.zip."
	echo " * Update the http://picketlink.org site."
	echo " * Announce in User Forum https://community.jboss.org/en/picketlink."
	echo " * Tell the world about it using Twitter, Google+, Facebook or any other social media."
	echo " * And congratulations ! :)"
	echo ""
    echo "==================================================="

	exit 0
}

usage()
{
cat << EOF
usage: $0 options

Use this script to release PicketLink versions.

OPTIONS:
   -s      Snapshot version number to update from.
   -v      New snapshot version number to update to, if undefined, defaults to the version number updated from.
   -r      Undo a previous release attempt and prepares the local repo for a new one.
   -da     Build and deploy artitacts.
   -pd     Build and publish docs.
EOF
}

SNAPSHOT_VERSION=""
RELEASE_VERSION=""

while true; do
  case "$1" in
	 -h)
		 usage
		 exit
		 ;;
	 -s )
		 SNAPSHOT_VERSION=$2
		 shift
		 shift
		 ;;
	 -v )
		 RELEASE_VERSION=$2
		 shift
		 shift
		 ;;
	 -r )
         rollback
         exit 0
		 ;;
	 -da )
         deploy_artifacts
         exit 0
		 ;;
	 -pd )
         publish_documentation
         exit 0
		 ;;
     * )
         break
         ;;
  esac
done

release
