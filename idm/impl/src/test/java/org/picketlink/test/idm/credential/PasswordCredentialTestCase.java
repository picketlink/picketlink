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

package org.picketlink.test.idm.credential;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for {@link UsernamePasswordCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PasswordCredentialTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests a successful validation.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        Assert.assertEquals(Status.VALID, credential.getStatus());
    }

    /**
     * <p>
     * Tests a unsuccessful validation.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword, new Date(), null);
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();

        badUserName.setUsername("Bad" + user.getLoginName());
        badUserName.setPassword(plainTextPassword);

        identityManager.validateCredentials(badUserName);

        Assert.assertEquals(Status.INVALID, badUserName.getStatus());

        UsernamePasswordCredentials badPassword = new UsernamePasswordCredentials();

        plainTextPassword = new Password("bad_password".toCharArray());
        
        badPassword.setUsername(user.getLoginName());
        badPassword.setPassword(plainTextPassword);

        identityManager.validateCredentials(badPassword);

        Assert.assertEquals(Status.INVALID, badPassword.getStatus());

    }
    
    /**
     * <p>
     * Tests a unsuccessful validation when the credential is expired.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        Calendar expirationDate = Calendar.getInstance();
        
        expirationDate.add(Calendar.MINUTE, -1);
        
        identityManager.updateCredential(user, plainTextPassword, new Date(), expirationDate.getTime());
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        Assert.assertEquals(Status.EXPIRED, credential.getStatus());
        
        Password newPassword = new Password("new_password".toCharArray());
        
        identityManager.updateCredential(user, newPassword);
        
        credential = new UsernamePasswordCredentials(user.getLoginName(), newPassword);
        
        identityManager.validateCredentials(credential);

        Assert.assertEquals(Status.VALID, credential.getStatus());
    }
    
    /**
     * <p>
     * Tests password updation.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password firstPassword = new Password("password1".toCharArray());

        identityManager.updateCredential(user, firstPassword);

        UsernamePasswordCredentials firstCredential = new UsernamePasswordCredentials(user.getLoginName(), firstPassword);

        identityManager.validateCredentials(firstCredential);

        Assert.assertEquals(Status.VALID, firstCredential.getStatus());
        
        Password secondPassword = new Password("password2".toCharArray());
        
        identityManager.updateCredential(user, secondPassword);
        
        UsernamePasswordCredentials secondCredential = new UsernamePasswordCredentials(user.getLoginName(), secondPassword);
        
        identityManager.validateCredentials(secondCredential);

        Assert.assertEquals(Status.VALID, secondCredential.getStatus());
        
        identityManager.validateCredentials(firstCredential);

        Assert.assertEquals(Status.INVALID, firstCredential.getStatus());
    }
}