package org.picketlink.idm.permission.acl.internal;

import java.io.Serializable;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.spi.PermissionVoter;

/**
 *
 * @author Shane Bryzak
 *
 */
public class PersistentPermissionVoter implements PermissionVoter {
    public VotingResult hasPermission(IdentityType recipient, Object resource, String operation) {
        // TODO implement
        return null;
    }

    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        // TODO implement
        return null;
    }
}
