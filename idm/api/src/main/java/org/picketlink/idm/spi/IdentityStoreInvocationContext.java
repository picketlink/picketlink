package org.picketlink.idm.spi;

import org.picketlink.idm.event.EventBridge;

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
}
