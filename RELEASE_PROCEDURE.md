#PicketLink Release Procedure#

##Pre-requisites##

Before releasing make sure your environment is properly configured as follows:

1. Make sure you have Git Flow installed and your local repository properly initialized.
    * https://github.com/nvie/gitflow/downloads
    * http://nvie.com/posts/a-successful-git-branching-model/
2. Make sure your ~/.m2/settings.xml is properly configure to allow you publish the artifacts to JBoss Nexus.
    
3. Make sure you're able to upload the documentation to docs.jboss.org/picketlink/3/.

4. If you are in a fork, check if you have a remote repository configured as the following:

    git remote add upstream https://github.com/picketlink/picketlink

5. Check for uncommited changes, before continuing.

##Release##

To release, simply run:  
      
      ./release.sh --current-version <old snapshot version> --version <release version>

  This will perform the following steps:
  
  1. Update your master and develop branchs with the latest changes and sync them.
  3. Check if all depencies are available in central
  4. Execute the maven-release-plugin in DryRun mode
  5. Perform the release
  6. Publish the artifacts into nexus
  6. Create a tag for the new version

##Closing the Staging repository in JBoss Nexus##

While you published your artifacts, they won't get automatically synced to the JBoss Community repository without a nod from you. You give the nod by promoting the staged artifacts through the Nexus web interface. 

Follow these steps to kick off the promotion:

1. Login to http://repository.jboss.org/nexus
2. Click on Staging Repositories link in the left hand navigation
3. Look for the staging activity with your username in the repository name with a status closed
4. Check the row and click Release or Drop

##Uploading documentation to docs.jboss.org##

You can also use the script to upload the documenration to docs.jboss.org. Just execute the following command:

    ./release.sh --upload-docs --current-version <old snapshot version> --version <release version>
    
After check if the documentation is available at http://docs.jboss.org/picketlink/3/

##If something goes wrong##

First, check the pre-requisites section for missed steps.

The release script generates a log file called pl-release.log. You can check it for more details about the release execution.

###Rollback###
You can always rollback the release in order to start again. For that use the following command:

    ./release.sh --rollback --current-version <old snapshot version> --version <release version>
