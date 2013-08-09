package org.picketlink.idm.jpa.model.sample.complex;

import org.picketlink.idm.jpa.model.sample.complex.entity.Email;
import org.picketlink.idm.jpa.model.sample.complex.entity.Person;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.Unique;

/**
 * @author  Pedro Igor
 */
public abstract class AbstractUser extends AbstractIdentityType implements Account {

    @Unique
    @AttributeProperty
    private String userName;

    @AttributeProperty
    private Integer failedLoginCount;

    @AttributeProperty
    private Integer loginCount;

    @AttributeProperty
    private Email email;

    @AttributeProperty
    private Person person;

    public AbstractUser(final String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
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

    public Email getEmail() {
        return email;
    }

    public void setEmail(final Email email) {
        this.email = email;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }
}
