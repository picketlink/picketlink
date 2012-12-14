package org.picketlink.idm.event;

import org.picketlink.idm.model.Group;

/**
 * This event is raised when a new {@link Group} is updated
 * 
 * @author Shane Bryzak
 */
public class GroupUpdatedEvent extends AbstractBaseEvent {
    private Group group;

    public GroupUpdatedEvent(Group role) {
        this.group = role;
    }

    public Group getGroup() {
        return group;
    }
}
