PicketLink
========================
http://picketlink.org

* Java EE Application Security
* Identity Management
* Federation
* Social
* REST Security
* Standard-based Security

This repository is no longer maintained
-------------------

This repository is no longer maintained and is archived. For more details, please take a look at http://picketlink.org/keycloak-merge-faq.

Building
-------------------

Ensure you have JDK 7 (or newer) installed

    java -version

If you already have Maven 3.1.0 (or newer) installed you can use it directly

    mvn clean install

Contributing
------------------
http://picketlink.org

Running the Testsuite
--------------------

All tests are enabled by default whe you execute a simple

    mvn clean install

Some modules provide specific profiles and system properties in order to run a specific set of integration or unit tests.

During a build (if tests are not skipped) the integration tests from *tests* are always executed.

Running the PicketLink IDM Testsuite
--------------------

For **PicketLink IDM**, the following profiles are available:

* idm-smoke-tests: Core tests that should be run as part of every build. Failures here will fail the build.

You also provide some additional system properties as follows:

* -Dtest.idm.configuration=[all,file,jpa,ldap,jdbc,ldap_jpa]: Specify which category of tests should be run. Usually, they are references to specific configurations for each identity store.
* -Dtest.idm.jpa.eclipselink.provider=true: Use EclipseLink instead of Hibernate when running the JPA identity store configuration tests.

Nightly Builds
---------------------

Nightly Builds are available at [http://repository-picketlink.forge.cloudbees.com/snapshot/](http://repository-picketlink.forge.cloudbees.com/snapshot/).

*Note:* Those artitacts are SNAPSHOT versions, they're only suitable to test new features, fixes, etc.

License
-------
* [ASLv2](http://www.apache.org/licenses/LICENSE-2.0)
