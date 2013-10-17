package org.picketlink.idm.drools;

import org.picketlink.idm.model.IdentityType;

/**
 *
 *
 * @author Shane Bryzak
 *
 */
public class PermissionCheck {

    private final IdentityType assignee;
    private final Object resource;
    private final String operation;

    private boolean granted = false;

    public PermissionCheck(IdentityType assignee, Object resource, String operation) {
        this.assignee = assignee;
        this.resource = resource;
        this.operation = operation;
    }

    public IdentityType getAssignee() {
        return assignee;
    }

    public Object getResource() {
        return resource;
    }

    public String getOperation() {
        return operation;
    }

    public void grant() {
        this.granted = true;
    }

    public boolean isGranted() {
        return granted;
    }
}
