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
package org.picketlink.test.identity.federation.api.saml.v2;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.util.DeflateUtil;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

/**
 * Unit test the DEFLATE compression
 * encoding/decoding cycles
 * @author Anil.Saldhana@redhat.com
 * @since Dec 11, 2008
 */
public class DeflateEncodingDecodingUnitTestCase extends TestCase
{
   public void testDeflateEncoding() throws Exception
   {
      AuthnRequestType authnRequest = (new SAML2Request()).createAuthnRequestType( 
            IDGenerator.create("ID_"), "http://sp", 
            "http://localhost:8080/idp","http://sp");
      
      StringWriter sw = new StringWriter();
      SAML2Request request = new SAML2Request();
      request.marshall(authnRequest, sw);
      byte[] deflatedMsg = DeflateUtil.encode(sw.toString());
      
      String base64Request = Base64.encodeBytes(deflatedMsg, Base64.DONT_BREAK_LINES);
      
      base64Request = URLEncoder.encode(base64Request, "UTF-8");
      
      //Decode
      String urlDecodedMsg = URLDecoder.decode(base64Request, "UTF-8");
      byte[] decodedMessage = Base64.decode(urlDecodedMsg);
      InputStream is = DeflateUtil.decode(decodedMessage); 
      AuthnRequestType decodedRequestType = request.getAuthnRequestType(is);
      
      assertNotNull(decodedRequestType); 
   } 
}