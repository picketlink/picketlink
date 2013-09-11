package org.picketlink.idm.internal;

import java.util.List;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.Permission;

/**
 * Default implementation of PermissionManager
 *
 * @author Shane Bryzak
 *
 */
public class ContextualPermissionManager extends AbstractIdentityContext implements PermissionManager {

    public ContextualPermissionManager(Partition partition, EventBridge eventBridge, IdGenerator idGenerator) {
        super(partition, eventBridge, idGenerator);
    }

    @Override
    public List<Permission> listPermissions(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Permission> listPermissions(Object resource, String permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean grantPermission(Permission permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean grantPermissions(List<Permission> permissions) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermission(Permission permission) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermissions(List<Permission> permissions) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clearPermissions(Object resource) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> listPermissionTypes(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }

}
