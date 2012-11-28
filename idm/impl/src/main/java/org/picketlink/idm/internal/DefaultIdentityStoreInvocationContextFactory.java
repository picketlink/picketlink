package org.picketlink.idm.internal;

import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

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
    public IdentityStoreInvocationContext createContext() {
        return new IdentityStoreInvocationContext(null, eventBridge);
    }

    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, emf.createEntityManager());
            }
        }
    }

}
