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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.security.Principal;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

/**
 * Common code useful for a SP
 * @author Anil.Saldhana@redhat.com
 * @since Jan 9, 2009
 */
public class SPUtil
{
   /**
    * Create a SAML2 auth request
    * @param serviceURL URL of the service
    * @param identityURL URL of the identity provider
    * @return   
    * @throws ConfigurationException 
    */
   public AuthnRequestType createSAMLRequest(String serviceURL, String identityURL) throws ConfigurationException
   {
      if (serviceURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "serviceURL");
      if (identityURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "identityURL");

      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      return saml2Request.createAuthnRequestType(id, serviceURL, identityURL, serviceURL);
   }

   public Principal createGenericPrincipal(Request request, String username, List<String> roles)
   {
      Context ctx = request.getContext();
      return new GenericPrincipal(ctx.getRealm(), username, null, roles);
   }
}