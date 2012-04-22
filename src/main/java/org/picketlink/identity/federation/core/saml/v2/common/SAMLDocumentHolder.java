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
package org.picketlink.identity.federation.core.saml.v2.common;

import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.w3c.dom.Document;

/**
 * A Holder class that can store
 * the SAML object as well as the corresponding
 * DOM object.
 * 
 * Users of this class need to make it threadsafe
 * by having one instance per thread (ThreadLocal)
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Aug 13, 2009
 */
public class SAMLDocumentHolder
{
   private SAML2Object samlObject;
   private Document samlDocument;
   
   public SAMLDocumentHolder(SAML2Object samlObject)
   { 
      this.samlObject = samlObject;
   }

   public SAMLDocumentHolder(Document samlDocument)
   { 
      this.samlDocument = samlDocument;
   }

   public SAMLDocumentHolder(SAML2Object samlObject, Document samlDocument)
   { 
      this.samlObject = samlObject;
      this.samlDocument = samlDocument;
   }
   
   public SAML2Object getSamlObject()
   {
      return samlObject;
   }

   public void setSamlObject(SAML2Object samlObject)
   {
      this.samlObject = samlObject;
   }

   public Document getSamlDocument()
   {
      return samlDocument;
   }

   public void setSamlDocument(Document samlDocument)
   {
      this.samlDocument = samlDocument;
   }
}