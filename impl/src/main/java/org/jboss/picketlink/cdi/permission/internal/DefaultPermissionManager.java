package org.jboss.picketlink.cdi.permission.internal;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.picketlink.cdi.permission.Permission;
import org.jboss.picketlink.cdi.permission.PermissionManager;
import org.jboss.picketlink.cdi.permission.PermissionQuery;
import org.jboss.picketlink.cdi.permission.spi.PermissionStore;

/**
 * Default implementation of the PermissionManager interface
 */
@ApplicationScoped
public class DefaultPermissionManager implements PermissionManager
{
    @Inject
    PermissionStore permissionStore;

    @Override
    public PermissionQuery createPermissionQuery()
    {
        PermissionQuery q = new PermissionQuery(permissionStore);
        return q;
    }

    @Override
    public void grantPermission(Permission permission)
    {
        permissionStore.grantPermission(permission);        
    }

    @Override
    public void grantPermissions(Collection<Permission> permission)
    {
        permissionStore.grantPermissions(permission);
    }

    @Override
    public void revokePermission(Permission permission)
    {
        permissionStore.revokePermission(permission);        
    }

    @Override
    public void revokePermissions(Collection<Permission> permissions)
    {
        permissionStore.revokePermissions(permissions);        
    }

}
