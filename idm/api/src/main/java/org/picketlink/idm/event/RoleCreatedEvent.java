package org.picketlink.idm.event;

import org.picketlink.idm.model.Role;

/**
 * This event is raised when a new {@link Role} is created
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RoleCreatedEvent extends AbstractBaseEvent {
    
    private Role role;

    public RoleCreatedEvent(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
