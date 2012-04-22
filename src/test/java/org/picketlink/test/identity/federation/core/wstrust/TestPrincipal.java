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
package org.picketlink.test.identity.federation.core.wstrust;

import java.security.Principal;

/**
 * <p>
 * Simple {@code Principal} implementation used in the test scenarios.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class TestPrincipal implements Principal
{
   private final String name;

   /**
    * <p>
    * Creates an instance of {@code TestPrincipal} with the specified name.
    * </p>
    * 
    * @param name a {@code String} representing the principal name.
    */
   public TestPrincipal(String name)
   {
      this.name = name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.Principal#getName()
    */
   public String getName()
   {
      return this.name;
   }
}
