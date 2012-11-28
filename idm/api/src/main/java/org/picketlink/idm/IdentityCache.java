package org.picketlink.idm;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.User;

/**
 * Storage for User, Group and Role instances to enable quick resolution of identity memberships.
 * 
 * @author Shane Bryzak
 */
public interface IdentityCache {
    /**
     * Returns the cached User object for the specified id, in the specified Realm.  If the User has
     * not previously been cached, returns null.
     * 
     * @param realm
     * @param id
     * @return
     */
    User lookupUser(Realm realm, String id);

    /**
     * Inserts the specified user into the cache, for the specified Realm.
     * 
     * @param realm
     * @param user
     */
    void putUser(Realm realm, User user);

    /**
     * 
     * @param identity
     */
    //void invalidate(IdentityType identity);
}
