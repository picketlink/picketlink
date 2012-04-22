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
package org.picketlink.identity.federation.ws.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 *         The RequestSecurityTokenCollection (RSTC) element is used to provide multiple RST requests. 
 *         One or more RSTR elements in an RSTRC element are returned in the response to the RequestSecurityTokenCollection.
 *       
 * 
 * <p>Java class for RequestSecurityTokenCollectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestSecurityTokenCollectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestSecurityToken" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}RequestSecurityTokenType" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class RequestSecurityTokenCollectionType implements SimpleCollectionUsage<RequestSecurityTokenType>
{
   protected List<RequestSecurityTokenType> requestSecurityToken = new ArrayList<RequestSecurityTokenType>();

   /**
    * Gets the value of the requestSecurityToken property. 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link RequestSecurityTokenType }
    * 
    * 
    */
   public List<RequestSecurityTokenType> getRequestSecurityToken()
   {
      return Collections.unmodifiableList(this.requestSecurityToken);
   }

   public void add(RequestSecurityTokenType t)
   {
      this.requestSecurityToken.add(t);
   }

   public boolean remove(RequestSecurityTokenType t)
   {
      return requestSecurityToken.remove(t);
   }
}