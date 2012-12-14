package org.picketlink.idm.event;

import org.picketlink.idm.model.Role;

/**
 * This event is raised when a new {@link Role} is updated
 * 
 * @author Shane Bryzak
 */
public class RoleUpdatedEvent extends AbstractBaseEvent {
    private Role role;

    public RoleUpdatedEvent(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
