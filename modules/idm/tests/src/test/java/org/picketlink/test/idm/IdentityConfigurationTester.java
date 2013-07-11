package org.picketlink.test.idm;

import org.picketlink.idm.PartitionManager;

/**
 * @author pedroigor
 */
public interface IdentityConfigurationTester {

    void beforeTest();
    void afterTest();

    PartitionManager getPartitionManager();

}
