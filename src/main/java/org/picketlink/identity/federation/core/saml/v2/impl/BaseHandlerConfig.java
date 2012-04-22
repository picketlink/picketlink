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
package org.picketlink.identity.federation.core.saml.v2.impl;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class BaseHandlerConfig
{
   protected Map<String, Object> params = new HashMap<String, Object>();

   /**
    * @see SAML2HandlerChainConfig#containsKey(String)
    */
   public boolean containsKey(String key)
   {
      return params.containsKey(key);
   }

   /**
    * @see SAML2HandlerChainConfig#getParameter(String)
    */
   public Object getParameter(String parameterName)
   {
      return params.get(parameterName);
   }

   /**
    * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2MapBasedConfig#addParameter(java.lang.String, java.lang.Object)
    */
   public void addParameter(String parameterName, Object value)
   {
      this.params.put(parameterName, value);
   }

   public void set(Map<String, Object> options)
   {
      this.params.putAll(options);
   }
}