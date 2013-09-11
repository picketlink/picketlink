package org.picketlink.idm.internal;

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.List;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.spi.StoreSelector;

/**
 * Default implementation of PermissionManager
 *
 * @author Shane Bryzak
 *
 */
public class ContextualPermissionManager extends AbstractIdentityContext implements PermissionManager {
    private final StoreSelector storeSelector;

    public ContextualPermissionManager(Partition partition, EventBridge eventBridge, IdGenerator idGenerator,
                                        StoreSelector storeSelector) {
        super(partition, eventBridge, idGenerator);
        this.storeSelector = storeSelector;
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
    public void grantPermission(Permission permission) {
        try {
            storeSelector.getStoreForPermissionOperation(this).grantPermission(permission);
        } catch (Exception e) {
            throw MESSAGES.permissionGrantFailed(permission, e);
        }
    }

    @Override
    public void grantPermissions(List<Permission> permissions) {
        // TODO Auto-generated method stub
    }

    @Override
    public void revokePermission(Permission permission) {
        // TODO Auto-generated method stub
    }

    @Override
    public void revokePermissions(List<Permission> permissions) {
        // TODO Auto-generated method stub
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
