package org.picketlink.idm.drools;

/**
 * Represents a permission for which the currently authenticated user is tested for.
 *
 * @author Shane Bryzak
 */
public class PermissionCheck {

    private final Object resource;
    private final String operation;

    private boolean granted = false;

    public PermissionCheck(Object resource, String operation) {
        this.resource = resource;
        this.operation = operation;
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
