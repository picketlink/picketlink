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
package org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;

/**
 * <p>Java class for XACMLAuthzDecisionStatementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XACMLAuthzDecisionStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Response"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class XACMLAuthzDecisionStatementType extends StatementAbstractType
{
   private static final long serialVersionUID = 1L;

   public static final String XSI_TYPE = "xacml-saml:XACMLAuthzDecisionStatementType";

   protected ResponseType response;

   protected RequestType request;

   /**
    * Gets the value of the response property.
    * 
    * @return
    *     possible object is
    *     {@link ResponseType }
    *     
    */
   public ResponseType getResponse()
   {
      return response;
   }

   /**
    * Sets the value of the response property.
    * 
    * @param value
    *     allowed object is
    *     {@link ResponseType }
    *     
    */
   public void setResponse(ResponseType value)
   {
      this.response = value;
   }

   /**
    * Gets the value of the request property.
    * 
    * @return
    *     possible object is
    *     {@link RequestType }
    *     
    */
   public RequestType getRequest()
   {
      return request;
   }

   /**
    * Sets the value of the request property.
    * 
    * @param value
    *     allowed object is
    *     {@link RequestType }
    *     
    */
   public void setRequest(RequestType value)
   {
      this.request = value;
   }
}