/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.internal;

import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * <p>A {@link org.picketlink.idm.spi.ContextInitializer} that populates the {@link org.picketlink.idm.spi.IdentityContext}
 * with a reference to an authenticated {@link org.picketlink.idm.model.Account}.</p>
 *
 * @author Pedro Igor
 */
public class AuthenticatedAccountContextInitializer implements ContextInitializer {

    @Inject
    private BeanManager beanManager;

    @Override
    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
        Identity identity = getIdentity();

        if (identity != null) {
            context.setParameter(IdentityContext.AUTHENTICATED_ACCOUNT, identity.getAccount());
        }

        Object credentials = getCredentials();

        if (credentials != null) {
            context.setParameter(IdentityContext.CREDENTIALS, credentials);
        }
    }

    /**
     * <p>We only get a reference to the {@link org.picketlink.Identity} bean if it was already installed and exists in its
     * context or scope. Context initializers can be invoked during the startup where no {@link javax.enterprise.context.SessionScoped}
     * or {@link javax.enterprise.context.RequestScoped} is already active.</p>
     *
     * <p>This check is important to avoid a stackoverflow when initializing IDM during app startup. In this case, CDI container tries
     * to initialize IDM every time we ask for an Identity bean if using injection. This only happens when loading beans from jboss modules.</p>
     *
     * @return
     */
    private Identity getIdentity() {
        Bean<Identity> bean = (Bean<Identity>) this.beanManager.getBeans(Identity.class).iterator().next();

        try {
            Context context = this.beanManager.getContext(bean.getScope());
            return context.get(bean);
        } catch (ContextNotActiveException ignore) {
            // we just ignore if the context is not active for the Identity bean. Usually, this happens when
            // executing operations during startup. Where no Session or Request context is active.
        }

        return null;
    }

    /**
     * <p>We only get a reference to the {@link org.picketlink.credential.DefaultLoginCredentials} bean if it was already installed and exists in its
     * context or scope. Context initializers can be invoked during the startup where no {@link javax.enterprise.context.RequestScoped} is already active.</p>
     *
     * @return
     */
    private Object getCredentials() {
        Bean<DefaultLoginCredentials> bean = (Bean<DefaultLoginCredentials>) this.beanManager.getBeans(DefaultLoginCredentials.class).iterator().next();

        try {
            Context context = this.beanManager.getContext(bean.getScope());
            DefaultLoginCredentials credentials = context.get(bean);

            if (credentials != null) {
                return credentials.getCredential();
            }
        } catch (ContextNotActiveException ignore) {
            // we just ignore if the context is not active. Usually, this happens when
            // executing operations during startup. Where no Request context is active.
        }

        return null;
    }

}
