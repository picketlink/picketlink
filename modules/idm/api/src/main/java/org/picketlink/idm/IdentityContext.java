package org.picketlink.idm;

import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityContext {
    IdentityTransaction getTransaction();

    Map<Object, Object> getProperties();

    IdentityManager createIdentityManager(Partition partition);
    IdentityManager defaultIdentityManager();

    /**
     * If there is not an active entity transaction, one is begun and rolled back.  Otherwise, if there is one
     * active already and an exception occurs, then setRollbackOnly is called on the active transaction.
     *
     * @param name
     * @return
     */
    Realm createRealm(String name);
    Tier createTier(String name);
    Realm findRealm(String name);
    Tier findTier(String name);
    void deleteRealm(Realm realm);
    void deleteTier(Tier tier);

    void close();
}