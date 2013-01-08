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

package org.picketlink.test.idm;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.X509Cert;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Test case for credential management. Tests the different {@link Credential} types implementations and usage.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class CredentialManagementTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests the {@link PlainTextPassword} usage.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testUsernameAndPassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = loadOrCreateUser("someUser", true);
        PlainTextPassword plainTextPassword = new PlainTextPassword("updated_password".toCharArray());
        
        identityManager.updateCredential(user, plainTextPassword, null, null);
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();
        
        credential.setUsername(user.getId());
        credential.setPassword(plainTextPassword);
        
        identityManager.validateCredentials(credential);
        
        Assert.assertEquals(Status.VALID, credential.getStatus());
        
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();
        
        badUserName.setUsername("Bad" + user.getId());
        badUserName.setPassword(plainTextPassword);
        
        identityManager.validateCredentials(badUserName);
        
        Assert.assertEquals(Status.INVALID, badUserName.getStatus());
        
        UsernamePasswordCredentials badPassword = new UsernamePasswordCredentials();
        
        badPassword.setUsername("Bad" + user.getId());
        badPassword.setPassword(plainTextPassword);
        
        identityManager.validateCredentials(badPassword);
        
        Assert.assertEquals(Status.INVALID, badPassword.getStatus());

    }
    
    /**
     * <p>
     * Tests the {@link X509Cert} usage.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testX509Certificate() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509Cert certCredential = new X509Cert(clientCert);
        X509CertificateCredentials credential = new X509CertificateCredentials(certCredential);
        
        User user = loadOrCreateUser(credential.getUsername(), true);
        
        identityManager.updateCredential(user, certCredential, null, null);
        
        identityManager.validateCredentials(credential);
        
        Assert.assertEquals(Status.VALID, credential.getStatus());
        
        X509Certificate badCert = getTestingCertificate("servercert2.txt");
        X509Cert badClientCertCredential = new X509Cert(badCert);
        X509CertificateCredentials badCredential = new X509CertificateCredentials(badClientCertCredential);
        
        badCredential.setUserName(user.getId());
        
        identityManager.validateCredentials(badCredential);
        
        Assert.assertEquals(Status.INVALID, badCredential.getStatus());
    }
    
    private X509Certificate getTestingCertificate(String fromTextFile) {
        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/" + fromTextFile);
        X509Certificate cert = null;

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load testing certificate.", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

}