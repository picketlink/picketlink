package org.picketlink.idm.credential;

import org.picketlink.idm.model.User;

/**
 * Abstract base class for Credentials
 *  
 * @author Shane Bryzak
 */
public abstract class AbstractBaseCredentials implements Credentials {

    private User validatedUser;
    private Status status = Status.UNVALIDATED;

    @Override
    public User getValidatedUser() {
        return validatedUser;
    }

    public void setValidatedUser(User user) {
        this.validatedUser = user;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
