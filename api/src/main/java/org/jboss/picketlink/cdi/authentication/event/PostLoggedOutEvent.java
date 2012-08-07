package org.jboss.picketlink.cdi.authentication.event;

import org.jboss.picketlink.idm.model.User;

/**
 * This event is raised just after the user un-authenticates
 * 
 * @author Shane Bryzak
 */
public class PostLoggedOutEvent 
{
    private User user;

    public PostLoggedOutEvent(User user)
    {
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
