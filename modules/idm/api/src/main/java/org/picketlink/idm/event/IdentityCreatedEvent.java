package org.picketlink.idm.event;

import org.picketlink.idm.model.IdentityType;

/**
 * This event is raised whenever a new IdentityType is created
 *
 * @author Shane Bryzak
 *
 */
public class IdentityCreatedEvent extends AbstractBaseEvent {
    private IdentityType identityType;

    public IdentityCreatedEvent(IdentityType identityType) {
        this.identityType = identityType;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }
}
