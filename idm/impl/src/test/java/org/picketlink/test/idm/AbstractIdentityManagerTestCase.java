package org.picketlink.test.idm;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

public class AbstractIdentityManagerTestCase {

    private IdentityManager identityManager;

    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }
    
    protected User getUser(String userName) {
        User user = new SimpleUser(userName);
        
        if (getIdentityManager().getUser(user.getId()) == null) {
            getIdentityManager().add(user);            
        }
        
        return getIdentityManager().getUser(userName);
    }

}