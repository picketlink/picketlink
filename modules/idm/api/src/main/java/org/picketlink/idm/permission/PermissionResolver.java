package org.picketlink.idm.permission;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.spi.PermissionVoter;
import org.picketlink.idm.permission.spi.PermissionVoter.VotingResult;

/**
 * Iterates through the configured PermissionVoter instances to determine whether a resource permission
 * is to be allowed or denied.
 *
 * @author Shane Bryzak
 */
public class PermissionResolver {
    private final List<PermissionVoter> voters;

    public PermissionResolver(List<PermissionVoter> voters) {
        this.voters = voters;
    }

    public boolean resolvePermission(IdentityType recipient, Object resource, String operation) {
        boolean permit = false;

        for (PermissionVoter voter : voters) {
            VotingResult result = voter.hasPermission(recipient, resource, operation);
            if (VotingResult.ALLOW.equals(result)) {
                permit = true;
            }
            else if (VotingResult.DENY.equals(result)) {
                return false;
            }
        }

        return permit;
    }

    public boolean resolvePermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        boolean permit = false;

        for (PermissionVoter voter : voters) {
            VotingResult result = voter.hasPermission(recipient, resourceClass, identifier, operation);
            if (VotingResult.ALLOW.equals(result)) {
                permit = true;
            }
            else if (VotingResult.DENY.equals(result)) {
                return false;
            }
        }

        return permit;
    }
}
