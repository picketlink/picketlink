package org.picketlink.internal;

import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import javax.inject.Inject;

/**
 * <p>A {@link org.picketlink.idm.spi.ContextInitializer} that sets an {@link javax.persistence.EntityManager}
 * into the {@link org.picketlink.idm.spi.IdentityContext}. This is specially useful when using an identity store that needs
 * access to the entity manager.</p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 *
 */
public class EntityManagerContextInitializer implements ContextInitializer {

    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Override
    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!context.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, this.entityManagerProvider.getEntityManager());
            }
        }
    }
}
