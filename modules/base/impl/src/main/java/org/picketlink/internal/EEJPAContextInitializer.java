package org.picketlink.internal;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EEJPAContextInitializer implements ContextInitializer {
    @Inject @PicketLink Instance<EntityManager> entityManagerInstance;

    @Override
    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (entityManagerInstance.isUnsatisfied()) {
                throw new SecurityConfigurationException("To use JPAIdentityStore you must provide an EntityManager producer method " +
                        "qualified with @org.picketlink.annotations.PicketLink.");
            } else if (!context.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManagerInstance.get());
            }
        }
    }
}
