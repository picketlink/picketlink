package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;

/**
 * Models an oAuth authorization
 * 
 * @author Shane Bryzak
 * 
 */
public class Authorization extends AbstractAttributedType implements Relationship {

    private static final long serialVersionUID = -8044173562668371515L;

    private User user;
    private Agent application;
    private String authorizationCode;
    private String accessToken;
    private String refreshToken;

    public Authorization(User user, Agent application, String authorizationCode, String accessToken, String refreshToken) {
        this.user = user;
        this.application = application;
        this.authorizationCode = authorizationCode;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    @RelationshipIdentity
    public User getUser() {
        return user;
    }

    @RelationshipIdentity
    public Agent getApplication() {
        return application;
    }

    @RelationshipAttribute
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    @RelationshipAttribute
    public String getAccessToken() {
        return accessToken;
    }

    @RelationshipAttribute
    public String getRefreshToken() {
        return refreshToken;
    }
}
