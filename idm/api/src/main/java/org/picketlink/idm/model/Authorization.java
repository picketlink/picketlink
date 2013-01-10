package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;

/**
 * Models an oAuth authorization
 * 
 * @author Shane Bryzak
 *
 */
public interface Authorization extends Relationship {
    @RelationshipIdentity
    User getUser();
    
    @RelationshipIdentity
    Agent getApplication();
    
    @RelationshipAttribute
    String getAuthorizationCode();
    
    @RelationshipAttribute
    String getAccessToken();
    
    @RelationshipAttribute
    String getRefreshToken();
}
