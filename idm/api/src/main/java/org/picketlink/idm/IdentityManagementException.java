package org.picketlink.idm;

/**
 * This exception is thrown if there is an error during an identity management operation.
 * 
 * @author Shane Bryzak
 */
public class IdentityManagementException extends SecurityException {
    private static final long serialVersionUID = -1607577358422916393L;

    public IdentityManagementException() {
        super();
    }

    public IdentityManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityManagementException(String message) {
        super(message);
    }

    public IdentityManagementException(Throwable cause) {
        super(cause);
    }
}
