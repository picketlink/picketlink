package org.picketlink.idm.event;

import org.picketlink.idm.model.User;

/**
 * This event is raised when a user is deleted
 * 
 * @author Shane Bryzak
 */
public class UserDeletedEvent extends AbstractBaseEvent {
    private User user;

    public UserDeletedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
