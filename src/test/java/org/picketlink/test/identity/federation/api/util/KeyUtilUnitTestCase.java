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
package org.picketlink.test.identity.federation.api.util;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.util.KeyUtil;
import org.w3c.dom.Element;

/**
 * Unit test the Key Util
 * @author Anil.Saldhana@redhat.com
 * @since Apr 29, 2009
 */
public class KeyUtilUnitTestCase extends TestCase
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

   public void testCertificate() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
      assertNotNull("Input keystore stream is not null", ksStream);
      
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(ksStream, keystorePass.toCharArray()); 
      assertNotNull("KeyStore is not null",ks);
      
      Certificate cert = ks.getCertificate(alias);
      assertNotNull("Cert not null", cert);
      
      Element keyInfo = KeyUtil.getKeyInfo(cert);
      assertNotNull(keyInfo);  
   }
}