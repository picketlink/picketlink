package org.picketlink.test.idm;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
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

    protected Role getRole(String name) {
        Role role = new SimpleRole(name);
        
        if (getIdentityManager().getRole(role.getName()) == null) {
            getIdentityManager().add(role);            
        }
        
        return getIdentityManager().getRole(name);
    }
    
    protected Group getGroup(String name, String parentGroupName) {
        Group parentGroup = null;
        
        if (getIdentityManager().getGroup(parentGroupName) == null) {
            parentGroup = new SimpleGroup(parentGroupName);
            
            getIdentityManager().add(parentGroup);
            
            parentGroup = new SimpleGroup(parentGroupName);
        }
        
        Group group = new SimpleGroup(name, parentGroup);
        
        if (getIdentityManager().getGroup(group.getName()) == null) {
            getIdentityManager().add(group);            
        }
        
        return getIdentityManager().getGroup(name);
    }

}