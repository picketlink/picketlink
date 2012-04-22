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
package org.picketlink.identity.federation.core.saml.v2.factories;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;

/**
 * Creates {@code SAML2HandlerChain}
 * @author Anil.Saldhana@redhat.com
 * @since Nov 6, 2009
 */
public class SAML2HandlerChainFactory
{
   public static SAML2HandlerChain createChain()
   {
      return new DefaultSAML2HandlerChain();
   }

   public static SAML2HandlerChain createChain(String fqn) throws ProcessingException
   {
      if (fqn == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "fqn");

      Class<?> clazz = SecurityActions.loadClass(SAML2HandlerChainFactory.class, fqn);
      if (clazz == null)
         throw new ProcessingException(ErrorCodes.CLASS_NOT_LOADED + fqn);

      try
      {
         return (SAML2HandlerChain) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new ProcessingException(ErrorCodes.CANNOT_CREATE_INSTANCE + fqn, e);
      }
   }
}