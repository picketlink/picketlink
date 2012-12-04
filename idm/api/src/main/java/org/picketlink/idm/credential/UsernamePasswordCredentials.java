package org.picketlink.idm.credential;

/**
 * Represents the credentials typically used by standard username/password authentication. 
 * 
 * @author Shane Bryzak
 */
public class UsernamePasswordCredentials implements LoginCredentials {

    private String username;

    private PasswordCredential password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void invalidate() {
        username = null;
        password.clear();
    }
}
