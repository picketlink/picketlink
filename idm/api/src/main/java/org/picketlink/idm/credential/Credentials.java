package org.picketlink.idm.credential;

import org.picketlink.idm.model.User;


/**
 * Represents the credentials the current user will use to authenticate, in addition to
 * providing information about the current state of the validation process. 
 * 
 * Only used during the authentication process
 * 
 * @author Shane Bryzak
 */
public interface Credentials {
    public enum Status {UNVALIDATED, IN_PROGRESS, INVALID, VALID};

    User getValidatedUser();

    Status getStatus();

    void invalidate();
}
