#!/bin/sh

VERBOSE=false

execute_cmd() {

    if [ -n "$VERBOSE" ]; then
        "$@"
    else
        "$@" > /dev/null
    fi
}

RELEASE_VERSION=""
DEVELOPMENT_VERSION=""

if [ "$1" == "-D"  ]; then
    shift
    VERBOSE="$1"
    shif
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
echo "Local repository updated."
echo ""

echo "Preparing to release using git flow."
echo "    Starting release $RELEASE_VERSION..."
execute_cmd git flow release start $RELEASE_VERSION
echo "Release started."
echo ""

echo "Preparing project to be released."
echo "    Executing maven-release-plugin in DryRun mode..."
#execute_cmd mvn release:prepare --batch-mode -Drelease -DdevelopmentVersion=$DEVELOPMENT_VERSION -DreleaseVersion=$RELEASE_VERSION -Dtag=vRELEASE_VERSION -DdryRun -DignoreSnapshots=true
