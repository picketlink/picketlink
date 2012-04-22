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
package org.picketlink.identity.federation.core.wstrust.writers;


import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.ADDRESS;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.ENDPOINT_REFERENCE;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.WSA_NS;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.WSA_PREFIX;

import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.ws.addressing.AttributedURIType;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;

/**
 * Write WS-Addressing Elements
 * @author Anil.Saldhana@redhat.com
 * @since Nov 5, 2010
 */
public class WSAddressingWriter
{
   private XMLStreamWriter writer;
   
   public WSAddressingWriter(XMLStreamWriter writer)
   {
      this.writer = writer;
   }
   
   public void write( EndpointReferenceType endpointReference) throws ProcessingException
   {
      StaxUtil.writeStartElement( writer, WSA_PREFIX, ENDPOINT_REFERENCE, WSA_NS );   
      StaxUtil.writeNameSpace( writer, WSA_PREFIX, WSA_NS );
       
      AttributedURIType attributedURI = endpointReference.getAddress();
      if( attributedURI != null )
      {
         String value = attributedURI.getValue();
         
         StaxUtil.writeStartElement( writer, WSA_PREFIX, ADDRESS, WSA_NS );  
         StaxUtil.writeCharacters( writer, value );
         StaxUtil.writeEndElement( writer ); 
      } 

      StaxUtil.writeEndElement( writer ); 
      StaxUtil.flush( writer ); 
   } 
}