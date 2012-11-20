package org.picketlink.idm.spi;

import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

public interface IdentityStoreInvocationContext {
    /**
     * 
     * @return
     */
    IdentityStoreSession getIdentityStoreSession();

    /**
     * 
     * @return
     */
    EventBridge getEventBridge();

    /**
     * Return the active Realm for this context
     * 
     * @return
     */
    Realm getRealm();

    /**
     * Return the active Tier for this context
     * 
     * @return
     */
    Tier getTier();
}
