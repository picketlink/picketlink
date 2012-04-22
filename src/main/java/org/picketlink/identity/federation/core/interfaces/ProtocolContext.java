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
package org.picketlink.identity.federation.core.interfaces;

import javax.xml.namespace.QName;

/**
 * Interface to indicate a protocol specific request context
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public interface ProtocolContext
{ 
   /**
    * An optional service name
    * @return
    */
   String serviceName();
   
   /**
    * A String that represents the token type
    * @return
    */
   String tokenType();
   
   /**
    * Return the QName of the token
    * @return 
    */
   QName getQName();
   
   /**
    * What family the context belongs to..
    * @see {@code SecurityTokenProvider#family()}
    * @see {@code FAMILY_TYPE}
    * @return
    */
   String family();
}