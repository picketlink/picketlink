package org.picketlink.idm.permission;

import java.io.Serializable;

import org.picketlink.idm.model.IdentityType;

/**
 * Represents a resource permission that is assigned to a specific IdentityType.
 *
 * @author Shane Bryzak
 */
public class IdentityPermission extends Permission {
    private IdentityType assignee;

    public IdentityPermission(Object resource, IdentityType assignee, String operation) {
        super(resource, operation);
        this.assignee = assignee;
    }

    public IdentityPermission(Class<?> resourceClass, Serializable resourceIdentifier, IdentityType assignee, String operation) {
        super(resourceClass, resourceIdentifier, operation);
        this.assignee = assignee;
    }

    /**
     * Returns the identity to which the permission is assigned.
     *
     * @return
     */
    public IdentityType getAssignee() {
        return assignee;
    }
}
