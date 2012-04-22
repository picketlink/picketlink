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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;

/**
 * Read a SAML-XACML request
 * 
 * @see {@code SAMLResponseParserTestCase#testXACMLDecisionStatements()}
 * @author Anil.Saldhana@redhat.com
 * @since Jan 8, 2009
 */
public class SAMLXACMLUnitTestCase 
{
   /**
    * Usage of samlp with xsi-type 
    */
   @Test
   public void testSAML_XACML_Read() throws Exception
   {
      String resourceName = "saml-xacml/saml-xacml-request.xml";  

      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream(resourceName);
      
      SAMLParser parser = new SAMLParser();
      RequestAbstractType req = (RequestAbstractType) parser.parse( is );
      assertNotNull(req);
      assertTrue( req instanceof XACMLAuthzDecisionQueryType );
      
      XACMLAuthzDecisionQueryType xadqt = (XACMLAuthzDecisionQueryType) req;
      RequestType requestType = xadqt.getRequest();
      assertNotNull(requestType);
   }
   
   /**
    * Usage of xacml-samlp
    */
   @Test
   public void testSAML_XACML_Read_2() throws Exception
   {      
      String resourceName = "saml-xacml/saml-xacml-request-2.xml";
      
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream(resourceName);
    
      SAMLParser parser = new SAMLParser();
      RequestAbstractType req = (RequestAbstractType) parser.parse( is );
      assertNotNull(req);
      assertTrue( req instanceof XACMLAuthzDecisionQueryType );
      
      XACMLAuthzDecisionQueryType xadqt = (XACMLAuthzDecisionQueryType) req;
      RequestType requestType = xadqt.getRequest();
      assertNotNull(requestType);
   }
}