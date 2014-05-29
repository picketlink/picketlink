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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.storage.X509CertificateStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public abstract class AbstractCertificateCredentialTestCase extends AbstractPartitionManagerTestCase {

    public AbstractCertificateCredentialTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testTrustedCertSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        credential.setTrusted(true);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        identityManager.updateCredential(user, clientCert);

        X509Certificate badCert = getTestingCertificate("servercert2.txt");
        X509CertificateCredentials badCredential = new X509CertificateCredentials(badCert);

        badCredential.setUserName(user.getId());

        identityManager.validateCredentials(badCredential);

        assertEquals(Credentials.Status.INVALID, badCredential.getStatus());
        assertNull(badCredential.getValidatedAccount());
    }

    @Test
    public void testAccountDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    public void testResetCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -1);

        identityManager.updateCredential(user, clientCert, new Date(), expirationDate.getTime());
        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.EXPIRED, credential.getStatus());

        X509Certificate badCert = getTestingCertificate("servercert2.txt");
        X509CertificateCredentials badCredential = new X509CertificateCredentials(badCert);

        identityManager.validateCredentials(badCredential);

        assertEquals(Credentials.Status.INVALID, badCredential.getStatus());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
    }

    @Test
    @Configuration(exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        Account user = createAccount(credential.getUsername());

        identityManager.updateCredential(user, clientCert);

        X509CertificateStorage currentStorage = identityManager.retrieveCurrentCredential(user, X509CertificateStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getBase64Cert());
    }

    protected abstract Account createAccount(String accountName);

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
