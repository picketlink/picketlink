package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class PlainTextPassword {

    private char[] password;

    public PlainTextPassword(char[] password) {
        this.password = password;
    }

    public char[] getPassword() {
        return password;
    }

    public void clear() {
        for (int i = 0; i < password.length; i++) {
            password[i] = 0x00;
        }
        password = null;
    }
}
