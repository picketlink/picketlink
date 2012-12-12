package org.picketlink.idm.event;

import org.picketlink.idm.model.User;

/**
 * This event is raised when a new User is updated
 * 
 * @author Shane Bryzak
 */
public class UserUpdatedEvent extends AbstractBaseEvent {
    private User user;

    public UserUpdatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
