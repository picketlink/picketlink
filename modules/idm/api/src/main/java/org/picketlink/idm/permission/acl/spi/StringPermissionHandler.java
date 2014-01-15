package org.picketlink.idm.permission.acl.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * An extremely trivial permission handler that allows permissions to be assigned to String resources
 *
 * @author Shane Bryzak
 */
public class StringPermissionHandler implements PermissionHandler {
    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return String.class.equals(resourceClass);
    }

    @Override
    public Serializable getIdentifier(Object resource) {
        checkResourceValid(resource);
        return (String) resource;
    }

    @Override
    public Class<?> unwrapResourceClass(Object resource) {
        checkResourceValid(resource);
        return String.class;
    }

    private void checkResourceValid(Object resource) {
        if (!(resource instanceof String)) {
            throw new IllegalArgumentException("Resource [" + resource + "] must be instance of String");
        }
    }

    @Override
    public Set<String> listClassOperations(Class<?> resourceClass) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> listInstanceOperations(Class<?> resourceClass) {
        return Collections.emptySet();
    }
}
