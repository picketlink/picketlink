/*
 * JBoss, Home of Professional Open Source.

 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.wstrust;

import java.security.Principal;
import java.util.Map;

import org.picketlink.identity.federation.ws.trust.ClaimsType;

/**
 * <p>
 * A {@code ClaimsProcessor} implementation is responsible for parsing the WS-Trust claims according to the specified
 * claims dialect and retrieving the attributes that correspond to the required claims. {@code ClaimsProcessor}s may
 * use the properties specified in the configuration to perform its job (for instance, to connect to an external LDAP
 * server or IDM system when retrieving the attributes). 
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface ClaimsProcessor
{
   /**
    * <p>
    * Initializes the {@code ClaimsProcessor} using the specified properties map.
    * </p>
    * 
    * @param properties a {@code Map<String, String>} that contains the properties that have been configured for
    * this {@code ClaimsProcessor}.
    */
   public void initialize(Map<String, String> properties);

   /**
    * <p>
    * Parses the specified claims according to the claims dialect and returns a {@code Map} of attributes that
    * correspond to the required claims. Implementing classes may get the attributes from a local context or from an
    * external system (like an LDAP server or IDM system).
    * </p>
    * 
    * @param claims a reference to the {@code ClaimsType} instance that contains the claims that must be inserted into
    * generated tokens as attributes.
    * @param principal the {@code Principal} to which the claims refer.
    * @return a {@code Map<String, Object>} of attributes that correspond to the required claims.
    * @throws WSTrustException if an error occurs while processing the claims.
    */
   public Map<String, Object> processClaims(ClaimsType claims, Principal principal) throws WSTrustException;
}
