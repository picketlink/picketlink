package org.picketlink.idm.event;

import org.picketlink.idm.model.User;

/**
 * This event is raised when a new User is created
 * 
 * @author Shane Bryzak
 */
public class UserCreatedEvent extends AbstractBaseEvent {
    private User user;

    public UserCreatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
