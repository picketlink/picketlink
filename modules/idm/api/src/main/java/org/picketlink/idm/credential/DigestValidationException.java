package org.picketlink.idm.credential;

/**
 *
 * @author Shane Bryzak
 */
public class DigestValidationException extends SecurityException {
    private static final long serialVersionUID = 1574461862980979583L;

    private boolean nonceExpired;

    public DigestValidationException(String msg) {
        super(msg);
    }

    public DigestValidationException(String msg, boolean nonceExpired) {
        super(msg);
        this.nonceExpired = nonceExpired;
    }

    public boolean isNonceExpired() {
        return nonceExpired;
    }
}
