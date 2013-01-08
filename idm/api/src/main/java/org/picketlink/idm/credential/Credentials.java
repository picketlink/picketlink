package org.picketlink.idm.credential;

import org.picketlink.idm.model.Agent;


/**
 * Represents the credentials the current user will use to authenticate, in addition to
 * providing information about the current state of the validation process. 
 * 
 * Only used during the authentication process
 * 
 * @author Shane Bryzak
 */
public interface Credentials {
    public enum Status {UNVALIDATED, IN_PROGRESS, INVALID, VALID, EXPIRED};

    Agent getValidatedAgent();

    Status getStatus();

    void invalidate();
}
