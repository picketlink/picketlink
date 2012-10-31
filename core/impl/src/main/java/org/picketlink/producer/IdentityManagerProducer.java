package org.picketlink.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;


/**
 * 
 * @author Shane Bryzak
 */
public class IdentityManagerProducer {

    // FIXME basic implementation just to get started, we need to rewrite this with proper configuration

    @Produces @ApplicationScoped
    public IdentityManager createIdentityManager() {

        return new DefaultIdentityManager(null, new JPAIdentityStore());
    }

}
