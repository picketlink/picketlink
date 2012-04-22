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
package org.picketlink.identity.federation.core.saml.v2.holders;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * Holds info about the issuer for saml messages creation
 * @author Anil.Saldhana@redhat.com
 * @param <JBossSAMLConstants>
 * @since Dec 10, 2008
 */
public class IssuerInfoHolder
{
   private NameIDType issuer;

   private String statusCodeURI = JBossSAMLURIConstants.STATUS_SUCCESS.get();

   private String samlVersion = JBossSAMLConstants.VERSION_2_0.get();

   public IssuerInfoHolder(NameIDType issuer)
   {
      if (issuer == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "issuer");
      this.issuer = issuer;
   }

   public IssuerInfoHolder(String issuerAsString)
   {
      if (issuerAsString == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "issuerAsString");
      issuer = new NameIDType();
      issuer.setValue(issuerAsString);
   }

   public NameIDType getIssuer()
   {
      return issuer;
   }

   public void setIssuer(NameIDType issuer)
   {
      this.issuer = issuer;
   }

   public String getStatusCode()
   {
      return statusCodeURI;
   }

   public void setStatusCode(String statusCode)
   {
      this.statusCodeURI = statusCode;
   }

   public String getSamlVersion()
   {
      return samlVersion;
   }

   public void setSamlVersion(String samlVersion)
   {
      this.samlVersion = samlVersion;
   }
}