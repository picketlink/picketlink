package org.picketlink.idm.event;

import org.picketlink.idm.model.Group;

/**
 * This event is raised when a Group is deleted
 * 
 * @author Shane Bryzak
 *
 */
public class GroupDeletedEvent extends AbstractBaseEvent {
    private Group group;
    
    public GroupDeletedEvent(Group group) {
        this.group = group;
    }
    
    public Group getGroup() {
        return group;
    }
}
