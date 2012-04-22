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
package org.picketlink.identity.federation.saml.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SAML Advice Type
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonAdviceType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected List<Object> advices = new ArrayList<Object>();

   /**
    * Add an advice
    * @param obj
    */
   public void addAdvice(Object obj)
   {
      advices.add(obj);
   }

   /**
    * Remove an advice
    * @param advice
    * @return
    */
   public boolean remove(Object advice)
   {
      return this.advices.remove(advice);
   }

   /**
    * Gets the advices. (Read only list)
    * @return {@link List} read only 
    */
   public List<Object> getAdvices()
   {
      return Collections.unmodifiableList(advices);
   }
}
