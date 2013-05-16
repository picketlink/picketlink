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

package org.picketlink.test.integration.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.authentication.internal.DefaultAuthenticatorSelector;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.test.integration.AbstractArquillianTestCase;
import org.picketlink.test.integration.ArchiveUtils;

/**
 * <p>Performs some tests against the {@link DefaultAuthenticatorSelector}.</p>
 * 
 * @author Pedro Igor
 * 
 */
@SuppressWarnings("unchecked")
public class AuthenticatorSelectorTestCase extends AbstractArquillianTestCase {

    @Inject
    private DefaultAuthenticatorSelector authenticatorSelector;
    
    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private Identity identity;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(AuthenticatorSelectorTestCase.class, JohnAuthenticator.class, MaryAuthenticator.class,
                PeterAuthenticator.class);
    }
    
    @After
    public void onFinish() {
        this.identity.logout();
    }

    @Test
    public void testSelectJohnAuthenticatorByName() {
        testAuthenticationFor("john");
    }

    @Test
    public void testSelectJohnAuthenticatorByType() {
        testAuthenticationFor("john", JohnAuthenticator.class);
    }

    @Test
    public void testSelectMaryAuthenticatorByName() {
        testAuthenticationFor("mary");
    }

    @Test
    public void testSelectMaryAuthenticatorByType() {
        testAuthenticationFor("mary", MaryAuthenticator.class);
    }

    @Test
    public void testSelectPeterAuthenticatorByName() {
        testAuthenticationFor("peter");
    }

    @Test
    public void testSelectPeterAuthenticatorByType() {
        testAuthenticationFor("peter", PeterAuthenticator.class);
    }

    private void testAuthenticationFor(String authenticatorName, Class<? extends AbstractAuthenticator>... authenticatorType) {
        if (authenticatorType == null || authenticatorType.length == 0) {
            this.authenticatorSelector.setAuthenticatorName(authenticatorName);
        } else {
            this.authenticatorSelector.setAuthenticatorClass(authenticatorType[0]);
        }
        
        this.credentials.setUserId(authenticatorName);
        
        this.identity.login();
        
        assertTrue(this.identity.isLoggedIn());
        assertEquals(authenticatorName, this.identity.getAgent().getLoginName());
    }

    public static abstract class AbstractAuthenticator extends BaseAuthenticator {

        @Inject
        private DefaultLoginCredentials credentials;

        @Override
        public void authenticate() {
            String validUserName = doGetValidUserName();

            if (this.credentials.getUserId().equals(validUserName)) {
                setStatus(AuthenticationStatus.SUCCESS);
                setAgent(new SimpleAgent(validUserName));
            }
        }

        protected String doGetValidUserName() {
            return getClass().getAnnotation(Named.class).value();
        }

    }

    @Named ("john")
    public static class JohnAuthenticator extends AbstractAuthenticator {

    }

    @Named ("mary")
    public static class MaryAuthenticator extends AbstractAuthenticator {

    }

    @Named ("peter")
    public static class PeterAuthenticator extends AbstractAuthenticator {

    }

}