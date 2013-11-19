package org.picketlink.idm.jpa.model.sample.complex.entity;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.IdentityType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Pedro Igor
 */
@IdentityManaged (IdentityType.class)
@Entity
public class IdentityObject implements Serializable {

    private static final long serialVersionUID = 4818314551287096647L;

    @Identifier
    @Id
    private String id;

    @IdentityClass
    private String type;

    @AttributeValue (name = "createdDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @AttributeValue
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @AttributeValue (name = "enabled")
    private boolean active;

    @OwnerReference
    @ManyToOne
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(final Company company) {
        this.company = company;
    }
}
