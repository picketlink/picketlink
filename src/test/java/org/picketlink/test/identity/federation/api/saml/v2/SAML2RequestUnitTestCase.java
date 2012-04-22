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

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request; 
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;


/**
 * Unit Test the SAMl2Request API
 * @author Anil.Saldhana@redhat.com
 * @since Jan 26, 2009
 */
public class SAML2RequestUnitTestCase extends TestCase
{
   public void testLogOut() throws Exception
   {
      SAML2Request saml2Request = new SAML2Request(); 
      LogoutRequestType lrt = saml2Request.createLogoutRequest("http://idp");
      assertNotNull("LogoutRequest is not null", lrt);
   }

}