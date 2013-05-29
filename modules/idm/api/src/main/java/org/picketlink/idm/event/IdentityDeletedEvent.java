package org.picketlink.idm.event;

import org.picketlink.idm.model.IdentityType;

/**
 * This event is raised whenever an IdentityType is deleted
 *
 * @author Shane Bryzak
 *
 */
public class IdentityDeletedEvent extends AbstractBaseEvent {
    private IdentityType identityType;

    public IdentityDeletedEvent(IdentityType identityType) {
        this.identityType = identityType;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }
}
