package org.picketlink.test.idm.util;

import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Shane Bryzak
 *
 */
public class JPAContextInitializer implements ContextInitializer {
    private EntityManagerFactory emf;

    public JPAContextInitializer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager(){
        return emf.createEntityManager();
    }

    @Override
    public void initContextForStore(IdentityContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, getEntityManager());
            }
        }
    }
}