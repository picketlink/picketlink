package org.picketlink.idm.credential;


/**
 * Represents the credentials typically used by standard username/password authentication. 
 * 
 * @author Shane Bryzak
 */
public class UsernamePasswordCredentials extends AbstractBaseCredentials {

    private String username;

    private PlainTextPassword password;

    public UsernamePasswordCredentials() {
        
    }
    
    public UsernamePasswordCredentials(String userName, PlainTextPassword password) {
        this.username = userName;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlainTextPassword getPassword() {
        return password;
    }

    public void setPassword(PlainTextPassword password) {
        this.password = password;
    }

    @Override
    public void invalidate() {
        setStatus(Status.INVALID);
        password.clear();
    }
}
