package org.picketlink.idm.permission.acl.spi;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.IdentityPermission;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.permission.spi.PermissionVoter;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Shane Bryzak
 */
public class PersistentPermissionVoter implements PermissionVoter {

    private final PartitionManager partitionManager;

    public PersistentPermissionVoter(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public VotingResult hasPermission(IdentityType recipient, Object resource, String operation) {
        if (recipient == null) {
            throw new IllegalArgumentException("recipient must not be null");
        }


        List<Permission> permissions = getPermissionManager(recipient).listPermissions(resource, operation);

        return checkPermission(recipient, permissions);
    }

    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        if (recipient == null) {
            throw new IllegalArgumentException("recipient must not be null");
        }

        List<Permission> permissions = getPermissionManager(recipient).listPermissions(resourceClass, identifier, operation);

        return checkPermission(recipient, permissions);
    }

    private PermissionManager getPermissionManager(IdentityType recipient) {
        return partitionManager.createPermissionManager(recipient.getPartition());
    }

    private VotingResult checkPermission(IdentityType recipient, List<Permission> permissions) {
        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        for (Permission permission : permissions) {
            if (permission instanceof IdentityPermission) {
                IdentityPermission idPermission = (IdentityPermission) permission;

                if (relationshipManager.inheritsPrivileges(recipient, idPermission.getAssignee())) {
                    return VotingResult.ALLOW;
                }
            }
        }

        return VotingResult.NOT_APPLICABLE;
    }
}
