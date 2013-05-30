package org.picketlink.idm.event;

import org.picketlink.idm.model.IdentityType;

/**
 * <p>This event is raised whenever a new IdentityType is created.</p>
 *
 * @author Shane Bryzak
 */
public class IdentityTypeDeletedEvent extends AbstractBaseEvent {

    private IdentityType identityType;

    public IdentityTypeDeletedEvent(IdentityType identityType) {
        this.identityType = identityType;
    }

    public IdentityType getIdentityType() {
        return this.identityType;
    }

}
