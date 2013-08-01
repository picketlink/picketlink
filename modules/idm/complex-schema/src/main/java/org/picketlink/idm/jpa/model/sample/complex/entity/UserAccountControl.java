package org.picketlink.idm.jpa.model.sample.complex.entity;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

/**
 */
@IdentityManaged (EmployeeUser.class)
@Entity
public class UserAccountControl implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @AttributeValue (name = "enabled")
    private boolean active;

    @AttributeValue
    private Date expirationDate;

    @AttributeValue
    private Integer failedLoginCount;

    @AttributeValue
    private Integer loginCount;

    @OwnerReference
    @ManyToOne
    private UserAccount account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(final Integer failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(final Integer loginCount) {
        this.loginCount = loginCount;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(final UserAccount account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        UserAccountControl other = (UserAccountControl) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
