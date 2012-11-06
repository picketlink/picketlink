package org.picketlink.idm.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.IdentityStoreSession;
import org.picketlink.idm.spi.JPAIdentityStoreSession;

/**
 * A default implementation of IdentityStoreInvocationContextFactory.
 * 
 * @author Shane Bryzak
 */
public class DefaultIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {
    private EntityManagerFactory emf;
    private EventBridge eventBridge;

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf) {
        this.emf = emf;
        this.eventBridge = new EventBridge() {

            @Override
            public void raiseEvent(Object event) {
                // by default do nothing
            }
        };
    }

    @Override
    public IdentityStoreInvocationContext getContext(IdentityStore store) {
        IdentityStoreSession session = new JPAIdentityStoreSession() {
            public EntityManager getEntityManager() {
                return emf.createEntityManager();
            }
        };

        return new DefaultIdentityStoreInvocationContext(session, eventBridge);
    }

}
