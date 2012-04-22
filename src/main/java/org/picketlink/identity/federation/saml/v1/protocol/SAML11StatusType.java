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
package org.picketlink.identity.federation.saml.v1.protocol;

import java.io.Serializable;

import org.picketlink.identity.federation.saml.common.CommonStatusDetailType;

/**
 * <complexType name="StatusType">
        <sequence>
            <element ref="samlp:StatusCode"/>
            <element ref="samlp:StatusMessage" minOccurs="0"/>
            <element ref="samlp:StatusDetail" minOccurs="0"/>
        </sequence>

    </complexType>
    
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11StatusType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected SAML11StatusCodeType statusCode;

   protected String statusMessage;

   protected CommonStatusDetailType statusDetail;

   public SAML11StatusCodeType getStatusCode()
   {
      return statusCode;
   }

   public void setStatusCode(SAML11StatusCodeType statusCode)
   {
      this.statusCode = statusCode;
   }

   public String getStatusMessage()
   {
      return statusMessage;
   }

   public void setStatusMessage(String statusMessage)
   {
      this.statusMessage = statusMessage;
   }

   public CommonStatusDetailType getStatusDetail()
   {
      return statusDetail;
   }

   public void setStatusDetail(CommonStatusDetailType statusDetail)
   {
      this.statusDetail = statusDetail;
   }

   public static SAML11StatusType successType()
   {
      SAML11StatusType success = new SAML11StatusType();
      success.setStatusCode(SAML11StatusCodeType.SUCCESS);
      return success;
   }
}