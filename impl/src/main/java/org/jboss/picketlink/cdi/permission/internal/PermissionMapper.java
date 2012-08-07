package org.jboss.picketlink.cdi.permission.internal;

import java.io.Serializable;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.picketlink.cdi.permission.PermissionResolver;
import org.jboss.picketlink.cdi.permission.PermissionResolver.PermissionStatus;

/**
 * Uses the available PermissionResolver instances to determine whether an application permission
 * is to be allowed or denied. 
 *
 */
public class PermissionMapper
{
    @Inject 
    private Instance<PermissionResolver> resolvers;
    
    public boolean resolvePermission(Object resource, String operation)
    {
        boolean permit = false;
         
        for (PermissionResolver resolver : resolvers)
        {
            PermissionStatus status = resolver.hasPermission(resource, operation);
            if (PermissionStatus.ALLOW.equals(status))
            {
                permit = true;
            }
            else if (PermissionStatus.DENY.equals(status))
            {
                return false;
            }
        }
        
        return permit;
    }
    
    public boolean resolvePermission(Class<?> resourceClass, Serializable identifier, String operation)
    {
        boolean permit = false;
        
        for (PermissionResolver resolver : resolvers)
        {
            PermissionStatus status = resolver.hasPermission(resourceClass, identifier, operation);
            if (PermissionStatus.ALLOW.equals(status))
            {
                permit = true;
            }
            else if (PermissionStatus.DENY.equals(status))
            {
                return false;
            }
        }
        
        return permit;        
    }
}
