package org.picketlink.idm.jpa.model.sample.complex.entity;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.complex.CustomerUser;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 */
@IdentityManaged ({EmployeeUser.class, CustomerUser.class})
@Entity
public class UserAccountControl implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @AttributeValue
    private Integer failedLoginCount;

    @AttributeValue
    private Integer loginCount;

    @OwnerReference
    @ManyToOne
    private IdentityObject account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public IdentityObject getAccount() {
        return account;
    }

    public void setAccount(final IdentityObject account) {
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
