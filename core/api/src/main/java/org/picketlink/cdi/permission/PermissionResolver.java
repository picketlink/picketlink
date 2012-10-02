package org.picketlink.cdi.permission;

import java.io.Serializable;

/**
 * A PermissionResolver may be used to determine access restrictions for application resources. For every
 * permission check the application performs, the hasPermission() method of each known PermissionResolver 
 * is invoked. For the permission check to succeed, at least one PermissionResolver must return a result of 
 * PermissionStatus.ALLOW.  If any PermissionResolver returns a result of PermissionStatus.DENY, the 
 * permission check is unsuccessful and the user is not allowed to carry out the requested operation.  
 * If a PermissionResolver does not explicitly allow or deny the permission, it should return a result of 
 * PermissionStatus.NOT_APPLICABLE.
 * 
 * @author Shane Bryzak
 *
 */
public interface PermissionResolver
{
    public enum PermissionStatus 
    {
        ALLOW, DENY, NOT_APPLICABLE
    }
        
    PermissionStatus hasPermission(Object resource, String operation);
    
    PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation);
}
