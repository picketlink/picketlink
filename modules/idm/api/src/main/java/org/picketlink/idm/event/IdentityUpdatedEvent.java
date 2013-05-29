package org.picketlink.idm.event;

import org.picketlink.idm.model.IdentityType;

/**
 * This event is raised whenever an IdentityType is updated
 *
 * @author Shane Bryzak
 *
 */
public class IdentityUpdatedEvent extends AbstractBaseEvent {
    private IdentityType identityType;

    public IdentityUpdatedEvent(IdentityType identityType) {
        this.identityType = identityType;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }
}
