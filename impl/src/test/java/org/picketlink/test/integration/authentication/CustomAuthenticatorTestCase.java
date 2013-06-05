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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.integration.ArchiveUtils;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Perform some authentication tests using a {@link CustomAuthenticator} that performs a simple authentication without
 * using the IDM subsystem.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class CustomAuthenticatorTestCase extends AbstractAuthenticatorTestCase {

    @Inject @PicketLink
    private CustomAuthenticator authenticator;

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(CustomAuthenticatorTestCase.class, CustomAuthenticator.class);
    }

    @Test
    @Override
    public void testSuccessfulPasswordBasedAuthentication() throws Exception {
        super.testSuccessfulPasswordBasedAuthentication();
        assertTrue(this.authenticator.isAuthenticationPerformed());
    }

    @Override
    protected User doLockUserAccount() {
        User user = this.authenticator.getUser(USER_NAME);
        
        user.setEnabled(false);
        
        return user;
    }
    
    @RequestScoped @PicketLink
    public static class CustomAuthenticator extends BaseAuthenticator {
        
        private Map<String, User> users = new HashMap<String, User>();
        
        @PostConstruct
        public void onInit() {
            this.users.put(USER_NAME, new SimpleUser(USER_NAME));
        }
        
        @Inject
        private DefaultLoginCredentials credentials;
        private boolean authenticationPerformed;

        @Override
        public void authenticate() {
            setStatus(AuthenticationStatus.FAILURE);

            if (this.credentials.getCredential() != null && this.credentials.getUserId() != null) {
                if (this.credentials.getUserId().equals("john") && this.credentials.getPassword().equals("mypasswd")) {
                    setStatus(AuthenticationStatus.SUCCESS);
                    setAgent(this.users.get(this.credentials.getUserId()));
                }
            }

            this.authenticationPerformed = true;
        }
        
        boolean isAuthenticationPerformed() {
            return this.authenticationPerformed;
        }
        
        User getUser(String loginName) {
            return this.users.get(loginName);
        }
    }
}