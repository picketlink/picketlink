#PicketLink Release Procedure#

##Pre-requisites##

Before releasing make sure your environment is properly configured as follows:

1. Make sure you have Git Flow installed and your local repository properly initialized.
    * https://github.com/nvie/gitflow/downloads
    * http://nvie.com/posts/a-successful-git-branching-model/
2. Make sure your ~/.m2/settings.xml is properly configure to allow you to publish the artifacts to JBoss Nexus.
    
3. Make sure you're able to upload the documentation to docs.jboss.org/picketlink/2/.

4. If you are in a fork, check if you have a remote repository configured as follows:

        git remote add upstream https://github.com/picketlink/picketlink

5. Check for uncommitted changes, before continuing.

##Release##

To release, simply run:  
      
      ./release.sh --snapshot <from snapshot version> --version <release version>

  This will perform the following steps:
  
  1. Update your master and develop branchs with the latest changes and sync them.
  3. Check if all depencies are available in central.
  4. Execute the maven-release-plugin in DryRun mode.
  5. Perform the release.
  6. Create a tag for the new version.
  7. Publish the artifacts into JBoss Nexus.
  8. Upload the documentation to docs.jboss.org.

###Post-Release steps###

####Closing the Staging repository in JBoss Nexus####

While you published your artifacts, they won't get automatically synced to the JBoss Community repository without a nod from you. You give the nod by promoting the staged artifacts through the Nexus web interface. 

Follow these steps to kick off the promotion:

1. Login to http://repository.jboss.org/nexus
2. Click on Staging Repositories link in the left hand navigation
3. Look for the staging activity with your username in the repository name with a status closed
4. Check the row and click Release or Drop

####Release PicketLink Federation Bindings####

You need to release the PicketLink Federation Bindings. This is an important step, without it the PicketLink Federation
release is incomplete.

Go to the following repository

    https://github.com/picketlink/picketlink-bindings

And use the release script to release the project.

####Upload the distribution package to downloads.jboss.org ####

You need access to upload to JBoss File Mgmt server. If you already have it, just check the following directory:

    /downloads_htdocs/picketlink

Where X.X.Final is the released version.

####Update PicketLink Site at picketlink.org ####

You need access to upload to JBoss File Mgmt server. If you already have it, just check the following directory:

    /www_htdocs/picketlink

####Update PicketLink Site at JBoss.org ####

The PicketLink site must be update with the new release. The site is located at http://jboss.org/picketlink.

You need a valid account in order to make the changes. The administration panel is at http://jboss.org/author.

The following changes need to be done:

* Update the announcement on the Main Page to reflect the new version.
* Update the Download Page with the new version.

####Tag the PicketLink Quickstarts ####

You need to create a tag for the PicketLink Quickstarts with the new released version.

The repository is located at https://github.com/jboss-developer/jboss-picketlink-quickstarts


##If something goes wrong##

First, check the pre-requisites section for missed steps.

The release script generates a log file called pl-release.log. You can check it for more details about the release execution.

###Rollback###
You can always rollback the release in order to start again. For that use the following command:

    ./release.sh --version <release version> --rollback