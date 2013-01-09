package org.picketlink.idm.credential;


/**
 * Represents the credentials typically used by standard username/password authentication. 
 * 
 * @author Shane Bryzak
 */
public class UsernamePasswordCredentials extends AbstractBaseCredentials {

    private String username;

    private EncodedPassword password;

    public UsernamePasswordCredentials() {
        
    }
    
    public UsernamePasswordCredentials(String userName, EncodedPassword password) {
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

    public EncodedPassword getPassword() {
        return password;
    }

    public UsernamePasswordCredentials setPassword(EncodedPassword password) {
        this.password = password;
        return this;
    }

    @Override
    public void invalidate() {
        setStatus(Status.INVALID);
        password.clear();
    }
}
