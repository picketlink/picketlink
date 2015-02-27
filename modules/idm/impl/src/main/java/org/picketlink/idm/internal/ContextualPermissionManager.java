package org.picketlink.idm.internal;

import org.picketlink.idm.IDMInternalMessages;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.internal.util.IdentityTypeUtil;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.IdentityPermission;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.spi.StoreSelector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default implementation of PermissionManager
 *
 * @author Shane Bryzak
 *
 */
public class ContextualPermissionManager implements PermissionManager {

    private final StoreSelector storeSelector;
    private final DefaultIdentityContext identityContext;
    private final PartitionManager partitionManager;

    public ContextualPermissionManager(Partition partition, DefaultPartitionManager defaultPartitionManager) {
        this.identityContext = new DefaultIdentityContext(partition,
                defaultPartitionManager.getEventBridge(),
                defaultPartitionManager.getIdGenerator(),
                defaultPartitionManager.getConfiguration().getPermissionHandlerPolicy());
        this.storeSelector = defaultPartitionManager.getStoreSelector();
        this.partitionManager = defaultPartitionManager;

        if (this.storeSelector.getStoreForPermissionOperation(identityContext) == null) {
            throw IDMInternalMessages.MESSAGES.permissionUnsupportedOperation();
        }
    }

    @Override
    public List<Permission> listPermissions(Object resource) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, resource));
    }

    @Override
    public List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, resourceClass, identifier));
    }

    @Override
    public List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier, String operation) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, resourceClass, identifier, operation));
    }

    @Override
    public List<Permission> listPermissions(Object resource, String operation) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, resource, operation));
    }

    @Override
    public List<Permission> listPermissions(Class<?> resource, String operation) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, (Object) resource, operation));
    }

    @Override
    public List<Permission> listPermissions(IdentityType identityType) {
        return resolveIdentityTypeReferences(storeSelector.getStoreForPermissionOperation(this.identityContext).listPermissions(this.identityContext, identityType));
    }

    @Override
    public void grantPermission(IdentityType assignee, Object resource, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this.identityContext).grantPermission(this.identityContext, assignee, resource, operation);
        } catch (Exception e) {
            throw MESSAGES.permissionGrantFailed(assignee, resource, operation, e);
        }
    }

    @Override
    public void revokePermission(IdentityType assignee, Object resource, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this.identityContext).revokePermission(this.identityContext, assignee, resource, operation);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeFailed(assignee, resource, operation, ex);
        }
    }

    @Override
    public void revokePermission(IdentityType assignee, Class<?> resourceclass, String operation) {
        try {
            storeSelector.getStoreForPermissionOperation(this.identityContext).revokePermission(this.identityContext, assignee, resourceclass, operation);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeFailed(assignee, resourceclass, operation, ex);
        }
    }

    @Override
    public void clearPermissions(Object resource) {
        try {
            storeSelector.getStoreForPermissionOperation(this.identityContext).revokeAllPermissions(this.identityContext, resource);
        } catch (Exception ex) {
            throw MESSAGES.permissionRevokeAllFailed(resource, ex);
        }
    }

    private List<Permission> resolveIdentityTypeReferences(List<Permission> permissions) {
        for (Permission permission : new ArrayList<Permission>(permissions)) {
            if (IdentityPermission.class.isInstance(permission)) {
                IdentityPermission identityPermission = (IdentityPermission) permission;
                IdentityType assignee = identityPermission.getAssignee();

                if (IdentityTypeReference.class.isInstance(assignee)) {
                    IdentityTypeReference identityTypeReference = (IdentityTypeReference) assignee;
                    IdentityType identityType = IdentityTypeUtil.resolveIdentityType(identityTypeReference.getId(),
                            identityTypeReference, this.partitionManager);

                    permissions.remove(permission);

                    Object resource = identityPermission.getResource();

                    if (resource != null) {
                        permissions.add(new IdentityPermission(resource, identityType, identityPermission.getOperation()));
                    } else {
                        permissions.add(new IdentityPermission(identityPermission.getResourceClass(),
                                identityPermission.getResourceIdentifier(), identityType, identityPermission.getOperation()));
                    }
                }
            }
        }

        return permissions;
    }
}
