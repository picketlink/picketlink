package org.picketlink.idm.jpa.model.sample.complex;

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.RelationshipQueryParameter;

import java.util.Date;

/**
 * @author Pedro Igor
 */
public class ApplicationAuthorization extends AbstractAttributedType implements Relationship {

    public static final RelationshipQueryParameter ACCOUNT = new RelationshipQueryParameter() {
        @Override
        public String getName() {
            return "account";
        }
    };

    public static final RelationshipQueryParameter APPLICATION = new RelationshipQueryParameter() {
        @Override
        public String getName() {
            return "application";
        }
    };

    private Account account;
    private Application application;

    @AttributeProperty
    private Date authorizationDate = new Date();

    @AttributeProperty
    private Date expirationDate;

    @AttributeProperty
    private String profileUrl;

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(final Application application) {
        this.application = application;
    }

    public Date getAuthorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(final Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(final String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
