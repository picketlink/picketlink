package org.picketlink.test.idm.testers;

import org.picketlink.idm.PartitionManager;

/**
 * @author pedroigor
 */
public interface IdentityConfigurationTester {

    void beforeTest();
    void afterTest();

    PartitionManager getPartitionManager();
}
