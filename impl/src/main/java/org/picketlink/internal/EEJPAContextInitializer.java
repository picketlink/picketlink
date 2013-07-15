package org.picketlink.internal;

import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EEJPAContextInitializer implements ContextInitializer {
    @Override
    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
        //TODO: Implement initContextForStore
    }
//    @Inject @PicketLink Instance<EntityManager> entityManagerInstance;
//
//    @Override
//    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
//        if (store instanceof JPAIdentityStore) {
//            if (entityManagerInstance.isUnsatisfied()) {
//                throw new SecurityConfigurationException("To use JPAIdentityStore you must provide an EntityManager producer method " +
//                        "qualified with @org.picketlink.annotations.PicketLink.");
//            } else if (!context.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
//                context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManagerInstance.get());
//            }
//        }
//    }

}
