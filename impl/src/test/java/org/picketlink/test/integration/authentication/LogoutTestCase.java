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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.sample.IdentityLocator;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.integration.ArchiveUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Pedro Igor
 *
 */
public class LogoutTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(LogoutTestCase.class);
    }
    
    @Before
    public void onSetup() {
        User john = IdentityLocator.getUser(this.identityManager, USER_NAME);

        if (john == null) {
            john = new User(USER_NAME);
            this.identityManager.add(john);
        }

        john.setEnabled(true);

        this.identityManager.update(john);

        Password password = new Password(USER_PASSWORD);

        this.identityManager.updateCredential(john, password);
    }
    
    @Test
    public void testLogout() throws Exception {
        super.credentials.setUserId(USER_NAME);
        super.credentials.setPassword(USER_PASSWORD);
        super.identity.login();
        
        assertTrue(super.identity.isLoggedIn());
        
        super.identity.logout();
        
        assertFalse(super.identity.isLoggedIn());
        assertNull(super.identity.getAccount());
    }
}
