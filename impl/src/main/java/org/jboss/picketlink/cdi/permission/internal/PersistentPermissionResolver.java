package org.jboss.picketlink.cdi.permission.internal;

import java.io.Serializable;

import org.jboss.picketlink.cdi.permission.PermissionResolver;
import org.jboss.picketlink.cdi.permission.PermissionResolver.PermissionStatus;

/**
 * A PermissionResolver implementation that provides ACL-style object permissions, backed by a database.
 *
 */
public class PersistentPermissionResolver implements PermissionResolver
{

    public PermissionStatus hasPermission(Object resource, String operation)
    {
        // TODO Auto-generated method stub
        return null;
    }


    public PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
