package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class PlainTextPassword {

    private char[] value;
    private boolean encodePassword = true;

    public PlainTextPassword(char[] value) {
        this.value = value;
    }

    public char[] getValue() {
        return value;
    }

    public boolean isEncodePassword() {
        return encodePassword;
    }

    public void setEncodePassword(boolean encodePassword) {
        this.encodePassword = encodePassword;
    }

    public void clear() {
        for (int i = 0; i < value.length; i++) {
            value[i] = 0x00;
        }
        value = null;
    }
}
