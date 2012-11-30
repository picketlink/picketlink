package org.picketlink.idm.event;

import org.picketlink.idm.model.Role;

/**
 * This event is raised when a Role is deleted
 *  
 * @author Shane Bryzak
 */
public class RoleDeletedEvent extends AbstractBaseEvent {

    private Role role;

    public RoleDeletedEvent(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
