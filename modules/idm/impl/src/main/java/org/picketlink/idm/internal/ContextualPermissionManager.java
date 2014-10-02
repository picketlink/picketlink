package org.picketlink.idm.internal;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;
import org.picketlink.idm.spi.StoreSelector;

import java.io.Serializable;
import java.util.List;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default implementation of PermissionManager
 *
 * @author Shane Bryzak
 *
 */
public class ContextualPermissionManager extends AbstractIdentityContext implements PermissionManager {
    private final StoreSelector storeSelector;

    public ContextualPermissionManager(Partition partition, EventBridge eventBridge, IdGenerator idGenerator,
                                        PermissionHandlerPolicy permissionHandlerPolicy, StoreSelector storeSelector) {
        super(partition, eventBridge, idGenerator, permissionHandlerPolicy);
        this.storeSelector = storeSelector;
    }

    @Override
    public List<Permission> listPermissions(Object resource) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, resource);
    }

    @Override
    public List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, resourceClass, identifier);
    }

    @Override
    public List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier, String operation) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, resourceClass, identifier, operation);
    }

    @Override
    public List<Permission> listPermissions(Object resource, String operation) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, resource, operation);
    }

    @Override
    public List<Permission> listPermissions(Class<?> resource, String operation) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, (Object) resource, operation);
    }

    @Override
    public List<Permission> listPermissions(IdentityType identityType) {
        return storeSelector.getStoreForPermissionOperation(this).listPermissions(this, identityType);
    }

    @Override
    public void grantPermission(IdentityType assignee, Object resource, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this).grantPermission(this, assignee, resource, operation);
        } catch (Exception e) {
            throw MESSAGES.permissionGrantFailed(assignee, resource, operation, e);
        }
    }

    @Override
    public void revokePermission(IdentityType assignee, Object resource, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this).revokePermission(this, assignee, resource, operation);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeFailed(assignee, resource, operation, ex);
        }
    }

    @Override
    public void revokePermission(IdentityType assignee, Class<?> resourceclass, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this).revokePermission(this, assignee, resourceclass, operation);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeFailed(assignee, resourceclass, operation, ex);
        }
    }

    @Override
    public void clearPermissions(Object resource) {
        try {
            storeSelector.getStoreForPermissionOperation(this).revokeAllPermissions(this, resource);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeAllFailed(resource, ex);
        }
    }
}
