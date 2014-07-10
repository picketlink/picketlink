PicketLink Release Procedure#

##Pre-requisites##

Before releasing make sure your environment is properly configured as follows:

1. Make sure your ~/.m2/settings.xml is properly configured to allow you to publish artifacts to JBoss Nexus. And that you have access to upload artifacts.
    
2. Make sure you're able to upload files to docs.jboss.org/picketlink.

3. Make sure you're able to upload files to downloads.jboss.org/picketlink.

4. Make sure you're able to upload files to picketlink.org.

5. Make sure you have cloned the picketlink site at https://github.com/picketlink/web-picketlink.org.

6. Make sure you have awestruct properly installed. Check this documentation https://docs.jboss.org/author/display/PLINK/Website+using+Awestruct.

7. If you are in a fork, check if you have a remote repository configured as follows:

        git remote add upstream https://github.com/picketlink/picketlink

8. Check for uncommitted changes, before continuing.

##Release##

To release, simply run:  
      
      ./release.sh --snapshot <from snapshot version> --version <release version>

  This will perform the following steps:
  
  1. Update your master branch with the latest changes from upstream.
  3. Check if all depencies are available in central.
  4. Change pom version to 
  5. Perform the release.
  6. Create a tag for the new version.
  7. Publish the artifacts into JBoss Nexus.
  8. Upload the documentation to docs.jboss.org.

###Post-Release steps###

####Release PicketLink Federation Bindings####

You need to release the PicketLink Federation Bindings. This is an important step, without it the PicketLink Federation
release is incomplete.

Go to the following repository

    https://github.com/picketlink/picketlink-bindings

And use the release script to release the project.

After that you must also follow the steps in **Closing the Staging Repository in JBoss Nexus**.

####Closing the Staging Repository in JBoss Nexus####

While you published your artifacts, they won't get automatically synced to the JBoss Community repository without a nod from you. You give the nod by promoting the staged artifacts through the Nexus web interface. 

Follow these steps to kick off the promotion:

1. Login to http://repository.jboss.org/nexus
2. Click on Staging Repositories link in the left hand navigation
3. Look for the staging activity with your username in the repository name with a status closed
4. Check the row and click Release or Drop

####Build and Upload the PicketLink Installer to downloads.jboss.org ####

The PicketLink Installer is important to let users configure their EAP and WindFly installation with the latest version.

For that, clone the following repository:

	https://github.com/picketlink/picketlink-installer

Change the version using the following command:

	perl -pi -e 's/2.6.0-SNAPSHOT/2.6.0.CR4/g' `find . -name \pom.xml`

Where you need to replace the current version (2.6.0-SNAPSHOT) with the version being released (2.6.0.CR4).

Now you need to build the project using 

	mvn clean package

This will generate a target/picketlink-installer-VERSION.zip file.

SSH to downloads.jboss.org and create a directory for the new version. Unlink the latest and create a new link using the new directory.

Copy the ZIP file to the latest directory.

####Update PicketLink Site at picketlink.org ####

Update your clone of picketlink.org. Change references to the previous version and announce the new one.

After that, publish the site.

####Update PicketLink Site at JBoss.org ####

The PicketLink site must be update with the new release. The site is located at http://jboss.org/picketlink.

You need a valid account in order to make the changes. The administration panel is at http://jboss.org/author.

The following changes need to be done:

* Update the announcement on the Main Page to reflect the new version.

You also need to open a thread in user forum announcing the new release.

####Tag the PicketLink Quickstarts ####

You need to create a tag for the PicketLink Quickstarts with the new released version.

The repository is located at https://github.com/jboss-developer/jboss-picketlink-quickstarts
