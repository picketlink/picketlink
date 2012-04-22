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

import java.security.GeneralSecurityException;

/**
 * <p>
 * Exception used to convey that an error has happened when handling a WS-Trust request message.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustException extends GeneralSecurityException
{
   private static final long serialVersionUID = -232066282004315310L;
   
   /**
    * <p>
    * Creates an instance of {@code WSTrustException} using the specified error message.
    * </p>
    * 
    * @param message the error message.
    */
   public WSTrustException(String message)
   {
      super(message);
   }
   
   /**
    * <p>
    * Creates an instance of {@code WSTrustException} using the specified error message and cause.
    * </p>
    * 
    * @param message the error message.
    * @param cause a {@code Throwable} representing the cause of the error. 
    */
   public WSTrustException(String message, Throwable cause)
   {
      super(message, cause);
   }
}