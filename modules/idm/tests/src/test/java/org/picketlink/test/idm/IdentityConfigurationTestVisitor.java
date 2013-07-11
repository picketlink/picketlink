package org.picketlink.test.idm;

import org.picketlink.idm.PartitionManager;

/**
 * @author pedroigor
 */
public interface IdentityConfigurationTestVisitor {

    void beforeTest();
    void afterTest();

    PartitionManager getPartitionManager();

}
