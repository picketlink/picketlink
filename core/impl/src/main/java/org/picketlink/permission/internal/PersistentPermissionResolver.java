package org.picketlink.permission.internal;

import java.io.Serializable;

import org.picketlink.permission.PermissionResolver;

/**
 * A PermissionResolver implementation that provides ACL-style object permissions, backed by a database.
 *
 */
public class PersistentPermissionResolver implements PermissionResolver
{

    public PermissionStatus hasPermission(Object resource, String operation)
    {
        return null;
    }


    public PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation)
    {
        return null;
    }
}
