package org.picketlink.idm.event;

import org.picketlink.idm.model.Group;

/**
 * This event is raised when a new {@link Group} is created
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class GroupCreatedEvent extends AbstractBaseEvent {
    
    private Group group;

    public GroupCreatedEvent(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }
}
