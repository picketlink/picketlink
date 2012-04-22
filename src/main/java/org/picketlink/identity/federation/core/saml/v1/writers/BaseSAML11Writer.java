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
package org.picketlink.identity.federation.core.saml.v1.writers;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 27, 2011
 */
public abstract class BaseSAML11Writer
{
   protected static String PROTOCOL_PREFIX = "samlp";

   protected static String ASSERTION_PREFIX = "saml";

   protected static String XACML_SAML_PREFIX = "xacml-saml";

   protected static String XACML_SAML_PROTO_PREFIX = "xacml-samlp";

   protected static String XSI_PREFIX = "xsi";

   protected XMLStreamWriter writer;

   public BaseSAML11Writer(XMLStreamWriter writer)
   {
      this.writer = writer;
   }
}