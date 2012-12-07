package org.picketlink.idm.credential;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.User;

/**
 * Abstract base class for Credentials
 *  
 * @author Shane Bryzak
 */
public abstract class AbstractBaseCredentials implements Credentials {

    private Agent validatedAgent;
    private Status status = Status.UNVALIDATED;

    @Override
    public Agent getValidatedAgent() {
        return validatedAgent;
    }

    public void setValidatedAgent(Agent agent) {
        this.validatedAgent = agent;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
