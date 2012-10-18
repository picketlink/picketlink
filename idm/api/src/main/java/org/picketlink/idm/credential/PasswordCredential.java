package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class PasswordCredential implements Credential {
    private String password;
    
    public PasswordCredential(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }    
}
