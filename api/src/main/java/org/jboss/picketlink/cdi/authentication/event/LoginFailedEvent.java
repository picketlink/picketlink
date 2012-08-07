package org.jboss.picketlink.cdi.authentication.event;

/**
 * This event is fired when an authentication attempt fails
 * 
 * @author Shane Bryzak
 */
public class LoginFailedEvent 
{
    private Exception loginException;

    public LoginFailedEvent(Exception loginException) 
    {
        this.loginException = loginException;
    }

    public Exception getLoginException() 
    {
        return loginException;
    }
}
