package org.picketlink.idm;

import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * Creates IdentityManager instances.
 *
 * @author Shane Bryzak
 *
 */
public interface IdentityManagerFactory {
    /**
     *
     * @return
     */
    IdentityManager createIdentityManager();

    /**
     *
     * @param partition
     * @return
     */
    IdentityManager createIdentityManager(Partition partition);

    /**
     *
     * @param name
     * @return
     */
    Realm createRealm(String name);

    /**
     *
     * @param name
     * @return
     */
    Realm getRealm(String name);

    /**
     *
     * @param realm
     */
    void removeRealm(Realm realm);

    /**
     *
     * @param name
     * @param parent
     * @return
     */
    Tier createTier(String name, Tier parent);

    /**
     *
     * @param name
     * @return
     */
    Tier getTier(String name);

    /**
     *
     * @param tier
     */
    void removeTier(Tier tier);
}
