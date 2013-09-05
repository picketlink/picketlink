/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.test.idm.credential;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.credential.storage.X509CertificateStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for {@link X509CertificateCredentials} type.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class CertificateCredentialTestCase extends AbstractPartitionManagerTestCase {

    public CertificateCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testTrustedCertSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        credential.setTrusted(true);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        identityManager.updateCredential(user, clientCert);

        X509Certificate badCert = getTestingCertificate("servercert2.txt");
        X509CertificateCredentials badCredential = new X509CertificateCredentials(badCert);

        badCredential.setUserName(user.getId());

        identityManager.validateCredentials(badCredential);

        assertEquals(Status.INVALID, badCredential.getStatus());
        assertNull(badCredential.getValidatedAccount());
    }
    
    @Test
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        
        user.setEnabled(false);
        
        identityManager.update(user);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    public void testResetCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -1);

        identityManager.updateCredential(user, clientCert, new Date(), expirationDate.getTime());
        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        X509Certificate badCert = getTestingCertificate("servercert2.txt");
        X509CertificateCredentials badCredential = new X509CertificateCredentials(badCert);

        identityManager.validateCredentials(badCredential);

        assertEquals(Status.INVALID, badCredential.getStatus());

        identityManager.updateCredential(user, clientCert);
        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    @Configuration (exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        X509Certificate clientCert = getTestingCertificate("servercert.txt");
        X509CertificateCredentials credential = new X509CertificateCredentials(clientCert);

        User user = createUser(credential.getUsername());

        identityManager.updateCredential(user, clientCert);

        X509CertificateStorage currentStorage = identityManager.retrieveCurrentCredential(user, X509CertificateStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getBase64Cert());
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