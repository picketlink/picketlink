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


import static org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants.APPLIES_TO;
import static org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants.WSP_PREFIX;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.WSP_NS;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;

/**
 * Write the WS-Policy Elements
 * @author Anil.Saldhana@redhat.com
 * @since Nov 5, 2010
 */
public class WSPolicyWriter
{
   private XMLStreamWriter writer;
   
   public WSPolicyWriter(XMLStreamWriter writer)
   {
      this.writer = writer;
   }
   /**
    * Write an {@code AppliesTo} to the stream
    * @param appliesTo
    * @param out
    * @throws ProcessingException
    */
   public void write( AppliesTo appliesTo) throws ProcessingException
   {
      StaxUtil.writeStartElement( writer, WSP_PREFIX, APPLIES_TO, WSP_NS );   
      StaxUtil.writeNameSpace( writer, WSP_PREFIX, WSP_NS );
      StaxUtil.writeCharacters(writer, "" ); //Seems like JDK bug - not writing end character
      
      List<Object> contentList = appliesTo.getAny();
      if( contentList != null )
      {
         for( Object content: contentList )
         {
            if( content instanceof EndpointReferenceType )
            {
               EndpointReferenceType endpointReference = (EndpointReferenceType) content;
               WSAddressingWriter wsAddressingWriter = new WSAddressingWriter(this.writer);
               wsAddressingWriter.write(endpointReference);
            }
         }
      }

      StaxUtil.writeEndElement( writer ); 
      StaxUtil.flush( writer );
   } 
}