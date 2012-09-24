package org.picketlink.cdi.permission;

import java.util.Collection;


/**
 * Manages user, role and group permissions. 
 * 
 * @author Shane Bryzak
 *
 */
public interface PermissionManager
{
    /**
     * 
     * @return A new PermissionQuery
     */
    PermissionQuery createPermissionQuery();
    
    /**
     * 
     * @param permission
     */
    void grantPermission(Permission permission);
    
    /**
     * 
     * @param permission
     */
    void grantPermissions(Collection<Permission> permission);
    
    /**
     * 
     * @param permission
     */
    void revokePermission(Permission permission);
    
    /**
     * 
     * @param permissions
     */
    void revokePermissions(Collection<Permission> permissions);
}
