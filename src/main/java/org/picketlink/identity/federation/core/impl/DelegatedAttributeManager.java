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
package org.picketlink.identity.federation.core.impl;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;

/**
 * An attribute manager that delegates to
 * another manager for attributes
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public class DelegatedAttributeManager implements AttributeManager
{
   private AttributeManager delegate = new EmptyAttributeManager();

   public DelegatedAttributeManager()
   {
   }

   /**
    * Set the delegate
    * @param manager
    */
   public void setDelegate(AttributeManager manager)
   {
      this.delegate = manager;
   }

   /**
    * Is the delegate set?
    * @return
    */
   public boolean isDelegateSet()
   {
      return this.delegate != null;
   }

   /**
    * @see AttributeManager#getAttributes(Principal, List)
    */
   public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys)
   {
      if (delegate == null)
         throw new RuntimeException(ErrorCodes.INJECTED_VALUE_MISSING + "Delegate");
      return delegate.getAttributes(userPrincipal, attributeKeys);
   }
}