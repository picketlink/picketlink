package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.DefaultIdentityCache;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.internal.DefaultIdGenerator;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class EEIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {

    @Inject @PicketLink Instance<EntityManager> entityManagerInstance;
    
    @Inject CDIEventBridge cdiEventBridge;
    
    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;
    
    public EEIdentityStoreInvocationContextFactory() {
        credentialHandlerFactory = new DefaultCredentialHandlerFactory();
        identityCache = new DefaultIdentityCache();
        idGenerator = new DefaultIdGenerator();
    }

    @Override
    public IdentityStoreInvocationContext createContext() {
        return new IdentityStoreInvocationContext(this.identityCache, cdiEventBridge, credentialHandlerFactory, idGenerator);
    }

    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManagerInstance.get());
            }
        }
    }

}
