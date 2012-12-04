package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class PasswordCredential implements Credential {
    private char[] password;
    
    public PasswordCredential(String password) {
        this.password = password.toCharArray();
    }

    public String getPassword() {
        return new String(password);
    }    
}
