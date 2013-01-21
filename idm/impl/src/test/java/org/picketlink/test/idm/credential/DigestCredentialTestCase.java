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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.internal.Digest;
import org.picketlink.idm.credential.internal.DigestCredentials;
import org.picketlink.idm.credential.internal.DigestUtil;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for {@link DigestCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class DigestCredentialTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, digestPassword);
        
        digestPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials credential = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, digestPassword);
        
        digestPassword.setDigest(DigestUtil.calculateA1("Bad" + user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials badUserName = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());

        digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("bad_somePassword");
        
        digestPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials badPassword = new DigestCredentials(digestPassword);
        
        identityManager.validateCredentials(badPassword);

        assertEquals(Status.INVALID, badPassword.getStatus());

    }
    
    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digest = new Digest();
        
        digest.setRealm("pl-idm");
        digest.setUsername(user.getLoginName());
        digest.setPassword("somePassword");
        
        Calendar expirationDate = Calendar.getInstance();
        
        expirationDate.add(Calendar.MINUTE, -1);
        
        identityManager.updateCredential(user, digest, new Date(), expirationDate.getTime());

        DigestCredentials credential = new DigestCredentials(digest);

        digest.setDigest(DigestUtil.calculateA1(user.getLoginName(), digest.getRealm(), digest.getPassword().toCharArray()));
        
        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());
        
        Digest newPassword = new Digest();
        
        newPassword.setRealm("pl-idm");
        newPassword.setUsername(user.getLoginName());
        newPassword.setPassword("someNewPassword");
        
        identityManager.updateCredential(user, newPassword);
        
        credential = new DigestCredentials(newPassword);
        
        newPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), newPassword.getRealm(), newPassword.getPassword().toCharArray()));
        
        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }
    
    @Test
    public void testMultipleRealms() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest realmAPassword = new Digest();

        realmAPassword.setRealm("Realm A");
        realmAPassword.setUsername(user.getLoginName());
        realmAPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, realmAPassword);

        Digest realmBPassword = new Digest();

        realmBPassword.setRealm("Realm B");
        realmBPassword.setUsername(user.getLoginName());
        realmBPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, realmBPassword);

        realmAPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmAPassword.getRealm(), realmAPassword.getPassword().toCharArray()));
        
        DigestCredentials realmACredentials = new DigestCredentials(realmAPassword);

        identityManager.validateCredentials(realmACredentials);

        assertEquals(Status.VALID, realmACredentials.getStatus());
        
        realmBPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmBPassword.getRealm(), realmBPassword.getPassword().toCharArray()));
        
        DigestCredentials realmBCredentials = new DigestCredentials(realmBPassword);

        identityManager.validateCredentials(realmBCredentials);

        assertEquals(Status.VALID, realmBCredentials.getStatus());

        realmBPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmAPassword.getRealm(), realmBPassword.getPassword().toCharArray()));
        
        realmBCredentials = new DigestCredentials(realmBPassword);

        identityManager.validateCredentials(realmBCredentials);

        assertEquals(Status.INVALID, realmBCredentials.getStatus());

    }
}