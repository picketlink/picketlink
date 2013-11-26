package org.picketlink.idm.permission.acl.spi;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.permission.spi.PermissionVoter;

/**
 *
 * @author Shane Bryzak
 *
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

        VotingResult result = VotingResult.NOT_APPLICABLE;

        PermissionManager pm = partitionManager.createPermissionManager(recipient.getPartition());
        RelationshipManager rm = partitionManager.createRelationshipManager();
        List<Permission> permissions = pm.listPermissions(resource, operation);

        for (Permission permission : permissions) {
            if (recipient.equals(permission.getAssignee())) {
                result = VotingResult.ALLOW;
                break;
            } else if (rm.inheritsPrivileges(recipient, permission.getAssignee())) {
                result = VotingResult.ALLOW;
                break;
            }
        }

        return result;
    }

    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        // TODO implement
        return null;
    }
}
