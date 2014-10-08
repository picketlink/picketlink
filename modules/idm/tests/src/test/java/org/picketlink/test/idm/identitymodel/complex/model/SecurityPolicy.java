package org.picketlink.test.idm.identitymodel.complex.model;

import java.io.Serializable;

/**
 * @author Pedro Igor
 */
public class SecurityPolicy implements Serializable {

    private static final long serialVersionUID = 8724267616548625460L;

    private String[] requiredCredentials;

    public String[] getRequiredCredentials() {
        return requiredCredentials;
    }

    public void setRequiredCredentials(final String[] requiredCredentials) {
        this.requiredCredentials = requiredCredentials;
    }
}
