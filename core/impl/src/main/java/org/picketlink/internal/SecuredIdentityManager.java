package org.picketlink.internal;

import java.util.Date;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Extends the default IdentityManager implementation by providing secured identity management operations
 * 
 * @author Shane Bryzak
 *
 */
public class SecuredIdentityManager extends DefaultIdentityManager implements IdentityManager {

    @Override
    public void add(IdentityType value) {
       
        // TODO Auto-generated method stub
        super.add(value);
    }

    @Override
    public void update(IdentityType value) {
        // TODO Auto-generated method stub
        super.update(value);
    }

    @Override
    public void remove(IdentityType value) {
        // TODO Auto-generated method stub
        super.remove(value);
    }

    @Override
    public void add(Relationship value) {
        // TODO Auto-generated method stub
        super.add(value);
    }

    @Override
    public void update(Relationship value) {
        // TODO Auto-generated method stub
        super.update(value);
    }

    @Override
    public void remove(Relationship value) {
        // TODO Auto-generated method stub
        super.remove(value);
    }

    @Override
    public Agent getAgent(String loginName) {
        return super.getAgent(loginName);
    }

    @Override
    public User getUser(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group getGroup(String groupId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addToGroup(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Role getRole(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantGroupRole(IdentityType identityType, Role role, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokeGroupRole(IdentityType identityType, Role role, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateCredential(Agent agent, Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateCredential(Agent agent, Object value, Date effectiveDate, Date expiryDate) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createRealm(Realm realm) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeRealm(Realm realm) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Realm getRealm(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createTier(Tier tier) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeTier(Tier tier) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Tier getTier(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityManager forRealm(Realm realm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityManager forTier(Tier tier) {
        // TODO Auto-generated method stub
        return null;
    }

}
