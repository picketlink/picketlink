package org.picketlink.idm.permission.acl.internal;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
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
        List<Permission> permissions = pm.listPermissions(resource, operation);

        // TODO we also need to support permissions inherited via relationships (i.e. group memberships, etc)
        for (Permission permission : permissions) {
            if (recipient.getId().equals(permission.getRecipient()) &&
                    recipient.getPartition().getId().equals(permission.getRecipient().getPartition().getId())) {
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
