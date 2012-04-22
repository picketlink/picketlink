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

import java.io.ByteArrayOutputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;


/**
 * Unit Test the SAML2 Authn Response factory
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class SAML2AuthnResponseUnitTestCase
{
   @Test
   public void testResponseTypeCreation() throws Exception
   { 
      //Initialize the Core STS
      PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
      sts.installDefaultConfiguration();

      IssuerInfoHolder issuerHolder = new IssuerInfoHolder("http://idp");
      issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());
      
      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue(IDGenerator.create());

      SAML2Response saml2Response = new SAML2Response();
      
      ResponseType rt = saml2Response.createResponseType("response111",
             new SPInfoHolder(), idp, issuerHolder);
      Assert.assertNotNull(rt);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      saml2Response.marshall(rt, baos);
   }   
}