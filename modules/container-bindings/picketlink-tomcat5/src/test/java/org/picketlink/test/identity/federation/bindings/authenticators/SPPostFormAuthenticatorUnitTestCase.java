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
package org.picketlink.test.identity.federation.bindings.authenticators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.security.cert.X509Certificate;

import org.junit.Test;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPPostFormAuthenticator;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContextClassLoader;

/**
 * Unit test the {@link SPPostFormAuthenticator}
 * @author Anil.Saldhana@redhat.com
 * @since Mar 1, 2011
 */
public class SPPostFormAuthenticatorUnitTestCase
{
   @Test
   public void testIDPMetadataFile() throws Exception
   {
      MockCatalinaContext ctx = new MockCatalinaContext();
      SPPostFormAuthenticator auth = new SPPostFormAuthenticator();
      auth.setContainer(ctx);

      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      URL configURL = tccl.getResource("config/test-idp-metadata-file-config.xml");
      URL[] urls = new URL[]
      {configURL};
      MockCatalinaContextClassLoader tcl = new MockCatalinaContextClassLoader(urls);
      tcl.associate("/WEB-INF/picketlink-idfed.xml", configURL.openStream());
      tcl.associate("/WEB-INF/picketlink-handlers.xml",
            tccl.getResourceAsStream("saml2/post/sp/employee/WEB-INF/picketlink-handlers.xml"));
      tcl.associate("/WEB-INF/testshib.org.idp-metadata.xml",
            tccl.getResourceAsStream("metadata/testshib.org.idp-metadata.xml"));
      tcl.setProfile("DUMMY");
      tcl.setDelegate(tccl);

      Thread.currentThread().setContextClassLoader(tcl);
      auth.testStart();
      assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SSO", auth.getIdentityURL());
      X509Certificate idpCert = auth.getIdpCertificate();
      assertNotNull(idpCert);
      assertEquals("CN=idp.testshib.org, O=TestShib, L=Pittsburgh, ST=Pennsylvania, C=US", idpCert.getIssuerDN()
            .getName());
   }
}