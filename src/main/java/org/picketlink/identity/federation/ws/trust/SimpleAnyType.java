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

import org.w3c.dom.Element;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 16, 2011
 */
public class SimpleAnyType implements SimpleCollectionUsage<Object>
{
   protected List<Object> any = new ArrayList<Object>();

   /**
    * Gets the value of the any property. 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Object }
    * {@link Element }
    * 
    */
   public List<Object> getAny()
   {
      return Collections.unmodifiableList(this.any);
   }

   public void add(Object t)
   {
      this.any.add(t);
   }

   public boolean remove(Object t)
   {
      return any.remove(t);

   }
}