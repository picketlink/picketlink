package org.picketlink.cdi.authentication.event;

import org.picketlink.idm.model.User;

/**
 * This event is raised just before the user un-authenticates
 * 
 * @author Shane Bryzak
 */
public class PreLoggedOutEvent 
{
    private User user;

    public PreLoggedOutEvent(User user)
    {
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }
}
