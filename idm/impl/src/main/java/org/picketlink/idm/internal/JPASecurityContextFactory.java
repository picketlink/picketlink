package org.picketlink.idm.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;

/**
 * Extends DefaultSecurityContextFactory with JPA support
 *
 * @author Shane Bryzak
 *
 */
public class JPASecurityContextFactory extends DefaultSecurityContextFactory {
    private EntityManagerFactory emf;

    public JPASecurityContextFactory(EntityManagerFactory emf){
        this(emf, null);
    }

    public JPASecurityContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf) {
        this(emf, chf, null);
    }

    public JPASecurityContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache) {
        this(emf, chf, identityCache, null, null);
    }

    public JPASecurityContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache,
                                                        EventBridge eventBridge, IdGenerator idGenerator) {
        super(chf, identityCache, eventBridge, idGenerator);
        this.emf = emf;
    }

    public EntityManager getEntityManager(){
         return emf.createEntityManager();
    }

    @Override
    public void initContextForStore(SecurityContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, getEntityManager());
            }
        }
    }
}
