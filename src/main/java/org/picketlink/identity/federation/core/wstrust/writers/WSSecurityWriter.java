/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust.writers;

import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.ID;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.USERNAME;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.USERNAME_TOKEN;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSSE_NS;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSSE_PREFIX;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSU_NS;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSU_PREFIX;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.ws.wss.secext.AttributedString;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;

/**
 * Write WS-Security Elements
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Nov 8, 2010
 */
public class WSSecurityWriter
{
   private final XMLStreamWriter writer;

   public WSSecurityWriter(XMLStreamWriter writer)
   {
      this.writer = writer;
   }

   public void write(UsernameTokenType usernameToken) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, WSSE_PREFIX, USERNAME_TOKEN, WSSE_NS);
      StaxUtil.writeNameSpace(writer, WSSE_PREFIX, WSSE_NS);

      String id = usernameToken.getId();
      if (StringUtil.isNullOrEmpty(id))
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "Id on the UsernameToken");

      QName wsuIDQName = new QName(WSU_NS, ID, WSU_PREFIX);
      StaxUtil.writeNameSpace(writer, WSU_PREFIX, WSU_NS);
      StaxUtil.writeAttribute(writer, wsuIDQName, id);

      AttributedString userNameAttr = usernameToken.getUsername();
      if (userNameAttr == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "User Name is null on the UsernameToken");

      StaxUtil.writeStartElement(writer, WSSE_PREFIX, USERNAME, WSSE_NS);
      StaxUtil.writeCharacters(writer, userNameAttr.getValue());
      StaxUtil.writeEndElement(writer);

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void writeLifetime(XMLGregorianCalendar created, XMLGregorianCalendar expires) throws ProcessingException
   {
      // write the created element.
      StaxUtil.writeStartElement(this.writer, WSU_PREFIX, WSTrustConstants.CREATED, WSU_NS);
      StaxUtil.writeNameSpace(this.writer, WSU_PREFIX, WSU_NS);
      StaxUtil.writeCharacters(this.writer, created.toXMLFormat());
      StaxUtil.writeEndElement(this.writer);

      // write the expires element.
      StaxUtil.writeStartElement(this.writer, WSU_PREFIX, WSTrustConstants.EXPIRES, WSU_NS);
      StaxUtil.writeNameSpace(this.writer, WSU_PREFIX, WSU_NS);
      StaxUtil.writeCharacters(this.writer, expires.toXMLFormat());
      StaxUtil.writeEndElement(this.writer);

      StaxUtil.flush(this.writer);
   }

   public void writeSecurityTokenReference(SecurityTokenReferenceType secRef) throws ProcessingException
   {
      Set<String> usedNamespaces = new HashSet<String>();
      usedNamespaces.add(WSSE_NS);

      StaxUtil.writeStartElement(writer, WSSE_PREFIX, WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE, WSSE_NS);
      StaxUtil.writeNameSpace(writer, WSSE_PREFIX, WSSE_NS);

      // write the id attribute, if available.
      if (secRef.getId() != null && secRef.getId() != "")
      {
         QName wsuIDQName = new QName(WSU_NS, ID, WSU_PREFIX);
         StaxUtil.writeNameSpace(writer, WSU_PREFIX, WSU_NS);
         StaxUtil.writeAttribute(writer, wsuIDQName, secRef.getId());
         usedNamespaces.add(WSU_NS);
      }

      // write all other attributes.
      for (Map.Entry<QName, String> entry : secRef.getOtherAttributes().entrySet())
      {
         QName key = entry.getKey();
         // check if the namespace needs to be written.
         if (!usedNamespaces.contains(key.getNamespaceURI()))
         {
            StaxUtil.writeNameSpace(this.writer, key.getPrefix(), key.getNamespaceURI());
            usedNamespaces.add(key.getNamespaceURI());
         }
         StaxUtil.writeAttribute(this.writer, key, entry.getValue());
      }

      // write the key identifier, if available.
      for (Object obj : secRef.getAny())
      {
         if (obj instanceof KeyIdentifierType)
         {
            KeyIdentifierType keyId = (KeyIdentifierType) obj;
            StaxUtil.writeStartElement(this.writer, WSSE_PREFIX, WSTrustConstants.WSSE.KEY_IDENTIFIER, WSSE_NS);
            StaxUtil.writeAttribute(this.writer, WSTrustConstants.WSSE.VALUE_TYPE, keyId.getValueType());
            StaxUtil.writeCharacters(this.writer, keyId.getValue());
            StaxUtil.writeEndElement(this.writer);
         }
      }

      StaxUtil.writeEndElement(this.writer);
      StaxUtil.flush(this.writer);
   }
}