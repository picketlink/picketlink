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
package org.picketlink.identity.federation.core.saml.v2.util;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;

import org.jboss.security.xacml.core.model.context.ObjectFactory;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.w3c.dom.Document;

/**
 * Utility for SAML and XACML
 * @author Anil.Saldhana@redhat.com
 * @since Dec 20, 2010
 */
public class SAMLXACMLUtil
{
   public final static String XACML_PKG_PATH = "org.jboss.security.xacml.core.model.context"; 
   
   public static JAXBContext getJAXBContext() throws JAXBException
   {
      return JAXBContext.newInstance( XACML_PKG_PATH );
   }
   
   public static Document getXACMLResponse( ResponseType responseType ) throws ProcessingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      JAXBElement<?> jaxb = (new ObjectFactory()).createResponse( responseType );
      
      StreamResult result = new StreamResult( baos );
      
      try
      {
         TransformerUtil.transform( SAMLXACMLUtil.getJAXBContext(), jaxb, result);
         return DocumentUtil.getDocument( new String( baos.toByteArray() ));
      }
      catch ( Exception e )
      {
         throw new ProcessingException( e );
      } 
   }
   
   public static Document getXACMLRequest( RequestType requestType ) throws ProcessingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Marshaller marshaller = getMarshaller();
      JAXBElement<?> jaxb = (new ObjectFactory()).createRequest( requestType );
      
      StreamResult result = new StreamResult( baos );
      
      try
      {
         TransformerUtil.transform( getJAXBContext(), jaxb, result);
         return DocumentUtil.getDocument( new String( baos.toByteArray() ));
      }
      catch ( Exception e )
      {
         throw new ProcessingException( e );
      } 
   }
}