package org.picketlink.idm.jpa.internal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;

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
    public void initContextForStore(SecurityContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, getEntityManager());
            }
        }
    }
}
