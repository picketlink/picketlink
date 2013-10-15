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
package org.picketlink.test.identity.federation.core.util;

import junit.framework.TestCase;
import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

/**
 * Test the KeyStore Util
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 15, 2009
 */
public class KeystoreUtilUnitTestCase extends TestCase {

    /**
     * Keystore (created 15Jan2009 and valid for 200K days) The Keystore has been created with the command (all in one
     * line)
     * keytool -genkey -alias servercert -keyalg RSA -keysize 1024 -dname
     * "CN=jbossidentity.jboss.org,OU=RD,O=JBOSS,L=Chicago,S=Illinois,C=US" -keypass test123 -keystore
     * jbid_test_keystore.jks
     * -storepass store123 -validity 200000
     */
    private String keystoreLocation = "keystore/jbid_test_keystore.jks";
    private String keystorePass = "store123";
    private String alias = "servercert";
    private String keyPass = "test123";

    /**
     * Generated a selfsigned cert keytool -selfcert -alias servercert -keypass test123 -keystore jbid_test_keystore.jks
     * -dname
     * "cn=jbid test, ou=JBoss, o=JBoss, c=US" -storepass store123
     */
    public void testSignatureValidationInvalidation() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
        assertNotNull("Input keystore stream is not null", ksStream);

        KeyStore ks = KeyStoreUtil.getKeyStore(ksStream, keystorePass.toCharArray());
        assertNotNull("KeyStore is not null", ks);

        // Check that there are aliases in the keystore
        Enumeration<String> aliases = ks.aliases();
        assertTrue("Aliases are not empty", aliases.hasMoreElements());

        PublicKey publicKey = KeyStoreUtil.getPublicKey(ks, alias, keyPass.toCharArray());
        assertNotNull("Public Key is not null", publicKey);

        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());

        String content = "Hello";
        byte[] sigValue = SignatureUtil.sign(content, privateKey);
        boolean isValid = SignatureUtil.validate(content.getBytes("UTF-8"), sigValue, publicKey);
        assertTrue("Valid sig?", isValid);
    }
}