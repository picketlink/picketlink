package org.picketlink.idm.jpa.model.sample.complex.entity;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA. User: pedroigor Date: 8/1/13 Time: 5:40 PM To change this template use File | Settings |
 * File Templates.
 */
@IdentityManaged (org.picketlink.idm.jpa.model.sample.complex.ApplicationAuthorization.class)
@Entity
public class ApplicationAuthorization implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @AttributeValue
    @Temporal(TemporalType.TIMESTAMP)
    private Date authorizationDate;

    @AttributeValue
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @AttributeValue
    private String profileUrl;

    @OwnerReference
    @ManyToOne
    private RelationshipTypeEntity owner;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public RelationshipTypeEntity getOwner() {
        return owner;
    }

    public void setOwner(final RelationshipTypeEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        ApplicationAuthorization other = (ApplicationAuthorization) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
