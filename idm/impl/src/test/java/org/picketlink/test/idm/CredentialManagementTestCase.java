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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Test;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.model.User;

/**
 * <p>Test case for credential management. Tests the different {@link Credential} types implementations and usage.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class CredentialManagementTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>Tests the {@link PasswordCredential} usage.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testUsernameAndPassword() throws Exception {
        User user = getIdentityManager().getUser("admin");
        
        assertTrue(getIdentityManager().validateCredential(user, new PasswordCredential("admin")));
        assertFalse(getIdentityManager().validateCredential(user, new PasswordCredential("bad_credential")));
        
        getIdentityManager().updateCredential(user, new PasswordCredential("updated_password"));
        
        assertFalse(getIdentityManager().validateCredential(user, new PasswordCredential("admin")));
        assertTrue(getIdentityManager().validateCredential(user, new PasswordCredential("updated_password")));
    }
    
    /**
     * <p>Tests the {@link X509CertificateCredential} usage.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testX509Certificate() throws Exception {
        User user = getIdentityManager().getUser("admin");
        X509Certificate clientCert = getTestingCertificate();
        
        getIdentityManager().updateCredential(user, new X509CertificateCredential(clientCert));
        
        assertTrue(getIdentityManager().validateCredential(user, new X509CertificateCredential(clientCert)));
    }
    
    private X509Certificate getTestingCertificate() {
        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/servercert.txt");
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
