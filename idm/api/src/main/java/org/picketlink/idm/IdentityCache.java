package org.picketlink.idm;

import org.picketlink.idm.model.IdentityType;

/**
 * Storage for User, Group and Role instances to enable quick resolution of identity memberships.
 * 
 * @author Shane Bryzak
 */
public interface IdentityCache {
    void invalidate(IdentityType identity);
}
