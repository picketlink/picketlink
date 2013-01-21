package org.picketlink.idm.credential.internal;

import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.Credentials.Status;


/**
 * Represents the credentials typically used by standard username/password authentication. 
 * 
 * @author Shane Bryzak
 */
public class UsernamePasswordCredentials extends AbstractBaseCredentials {

    private String username;

    private Password password;

    public UsernamePasswordCredentials() {
        
    }
    
    public UsernamePasswordCredentials(String userName, Password password) {
        this.username = userName;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public UsernamePasswordCredentials setUsername(String username) {
        this.username = username;
        return this;
    }

    public Password getPassword() {
        return password;
    }

    public UsernamePasswordCredentials setPassword(Password password) {
        this.password = password;
        return this;
    }

    @Override
    public void invalidate() {
        setStatus(Status.INVALID);
        password.clear();
    }
}
