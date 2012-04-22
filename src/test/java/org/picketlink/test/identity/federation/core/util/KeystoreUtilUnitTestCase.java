/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.core.util;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;

/**
 * Test the KeyStore Util
 * @author Anil.Saldhana@redhat.com
 * @since Jan 15, 2009
 */
public class KeystoreUtilUnitTestCase extends TestCase
{

   /**
    * Keystore (created 15Jan2009 and valid for 200K days)
    * The Keystore has been created with the command (all in one line) 
keytool -genkey -alias servercert 
                -keyalg RSA 
                -keysize 1024 
                -dname "CN=jbossidentity.jboss.org,OU=RD,O=JBOSS,L=Chicago,S=Illinois,C=US" 
                -keypass test123 
                -keystore jbid_test_keystore.jks 
                -storepass store123 
                -validity 200000
    */
   private String keystoreLocation = "keystore/jbid_test_keystore.jks";
   private String keystorePass = "store123";
   private String alias = "servercert";
   private String keyPass = "test123";
   
   
   /**
    Generated a selfsigned cert
    keytool -selfcert 
             -alias servercert 
             -keypass test123 
             -keystore jbid_test_keystore.jks  
             -dname "cn=jbid test, ou=JBoss, o=JBoss, c=US" 
             -storepass store123 
    */
   public void testSignatureValidationInvalidation() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
      assertNotNull("Input keystore stream is not null", ksStream);
      
      KeyStore ks = KeyStoreUtil.getKeyStore(ksStream, keystorePass.toCharArray());
      assertNotNull("KeyStore is not null",ks);
         
      //Check that there are aliases in the keystore
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