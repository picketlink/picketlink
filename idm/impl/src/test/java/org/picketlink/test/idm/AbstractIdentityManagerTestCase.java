package org.picketlink.test.idm;

import org.picketlink.idm.IdentityManager;

public class AbstractIdentityManagerTestCase {

    private IdentityManager identityManager;

    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

}