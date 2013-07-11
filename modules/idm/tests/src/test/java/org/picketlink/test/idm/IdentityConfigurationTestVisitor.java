package org.picketlink.test.idm;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.internal.DefaultPartitionManager;

/**
 * @author pedroigor
 */
public interface IdentityConfigurationTestVisitor {

    PartitionManager buildConfiguration();

    void beforeTest();
    void afterTest();

}
