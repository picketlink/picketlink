package org.picketlink.idm.event;

import org.picketlink.idm.model.Relationship;

/**
 * This event is raised when a new {@link Relationship} is created
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RelationshipCreatedEvent extends AbstractBaseEvent {
    
    private Relationship relationship;

    public RelationshipCreatedEvent(Relationship relationship) {
        this.relationship = relationship;
    }
    
    public Relationship getRelationship() {
        return relationship;
    }
}
