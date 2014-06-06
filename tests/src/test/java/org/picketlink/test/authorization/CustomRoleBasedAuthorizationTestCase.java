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

package org.picketlink.test.authorization;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

import javax.inject.Inject;

import static org.junit.Assert.fail;

/**
 * <p>
 * Perform some authentication tests using the {@link org.picketlink.authentication.internal.IdmAuthenticator}, which is the default {@link java.net.Authenticator}.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class CustomRoleBasedAuthorizationTestCase extends AbstractAuthorizationTestCase {

    @Inject
    protected AnnotationProtectedBean protectedBean;

    @Deployment
    public static WebArchive deploy() {
        return create(CustomRoleBasedAuthorizationTestCase.class, AnnotationProtectedBean.class);
    }

    @Before
    public void onSetup() throws Exception {
        User john = BasicModel.getUser(this.identityManager, USER_NAME);

        if (john == null) {
            john = new User(USER_NAME);

            this.identityManager.add(john);

            this.identityManager.updateCredential(john, new Password(USER_PASSWORD));

            MyCustomRole tester = new MyCustomRole("Tester");

            this.identityManager.add(tester);

            relationshipManager.add(new MyCustomGrant(john, tester));;

            this.userTransaction.commit();
        }
    }

    @Test
    public void testSuccessfulInvocationWithRequiredRole() throws Exception {
        performAuthentication();
        this.protectedBean.protectedWithRequiredRole();
    }

    @Test
    public void testGrantRequiredRole() throws Exception {
        performAuthentication();

        try {
            this.protectedBean.protectedWithRequiredInvalidRole();
            fail();
        } catch (Exception e) {
            if (!AccessDeniedException.class.isInstance(e) && !AccessDeniedException.class.isInstance(e.getCause())) {
                fail();
            }
        }

        MyCustomRole role = new MyCustomRole("Invalid Role");

        this.identityManager.add(role);

        this.relationshipManager.add(new MyCustomGrant(this.identity.getAccount(), role));

        this.protectedBean.protectedWithRequiredInvalidRole();
    }
}