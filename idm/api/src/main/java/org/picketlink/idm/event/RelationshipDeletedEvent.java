package org.picketlink.idm.event;

import org.picketlink.idm.model.Relationship;

/**
 * This event is raised when a new {@link Relationship} is deleted
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RelationshipDeletedEvent extends AbstractBaseEvent {
    
    private Relationship relationship;

    public RelationshipDeletedEvent(Relationship relationship) {
        this.relationship = relationship;
    }
    
    public Relationship getRelationship() {
        return relationship;
    }
}
