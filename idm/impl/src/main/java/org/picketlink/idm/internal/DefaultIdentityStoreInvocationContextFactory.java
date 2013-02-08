package org.picketlink.idm.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.DefaultIdentityCache;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.PartitionStore;

/**
 * A default implementation of IdentityStoreInvocationContextFactory.
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 */
public class DefaultIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {
    private EntityManagerFactory emf;
    private EventBridge eventBridge;
    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;

    // FIXME Bad!! we can't do this, this class is multi-threaded!
    private EntityManager entityManager;

    public static DefaultIdentityStoreInvocationContextFactory DEFAULT = new DefaultIdentityStoreInvocationContextFactory(null, new DefaultCredentialHandlerFactory());

    public DefaultIdentityStoreInvocationContextFactory(){
        this.eventBridge = new EventBridge() {

            @Override
            public void raiseEvent(Object event) {
                // by default do nothing
            }
        };
        this.credentialHandlerFactory = new DefaultCredentialHandlerFactory();
        this.identityCache = new DefaultIdentityCache();
        this.idGenerator = new DefaultIdGenerator();
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf){
        this();
        this.emf = emf;
    }
    
    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf) {
        this(emf);
        this.credentialHandlerFactory = chf;
    }
    
    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache) {
        this(emf, chf);
        this.identityCache = identityCache;
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache,
                                                        EventBridge eventBridge, IdGenerator idGenerator) {
        this(emf, chf, identityCache);
        this.idGenerator = idGenerator;

        if (eventBridge != null) {
            this.eventBridge = eventBridge;
        }
    }

    @Override
    public IdentityStoreInvocationContext createContext() {
        return new IdentityStoreInvocationContext(this.identityCache, eventBridge, credentialHandlerFactory, idGenerator);
    }

    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, getEntityManager());
            }
        }
    }

    public EntityManager getEntityManager(){
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }

    public void setEntityManager(EntityManager em){
        this.entityManager = em;
    }

}