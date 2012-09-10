package org.jboss.picketlink.cdi.authentication.event;

/**
 * This event is fired when an authentication attempt fails
 * 
 * @author Shane Bryzak
 */
public class LoginFailedEvent 
{
    private Throwable loginException;

    public LoginFailedEvent(Throwable loginException) 
    {
        this.loginException = loginException;
    }

    public Throwable getLoginException() 
    {
        return loginException;
    }
}
