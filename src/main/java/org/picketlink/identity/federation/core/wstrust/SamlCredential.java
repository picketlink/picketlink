/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust;

import java.io.Serializable;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Credential that wraps a SAML Assertion.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * 
 */
public final class SamlCredential implements Serializable
{
   private static final long serialVersionUID = -8496414959425288835L;

   private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

   private final String assertion;

   public SamlCredential(final Element assertion)
   {
      if (assertion == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "assertion");

      this.assertion = SamlCredential.assertionToString(assertion);
   }

   public SamlCredential(final String assertion)
   {
      if (StringUtil.isNullOrEmpty(assertion))
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "assertion");

      this.assertion = assertion;
   }

   public String getAssertionAsString()
   {
      return assertion;
   }

   public Element getAssertionAsElement() throws ProcessingException
   {
      return SamlCredential.assertionToElement(assertion);
   }

   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
         return true;

      if (!(obj instanceof SamlCredential))
         return false;

      final SamlCredential that = (SamlCredential) obj;
      return this.assertion.equals(that.assertion);
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + assertion.hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      return "SamlCredential[" + assertion + "]";
   }

   public static Element assertionToElement(final String assertion) throws ProcessingException
   {
      try
      {
         Document document = DocumentUtil.getDocument(assertion);
         return (Element) document.getFirstChild();
      }
      catch (final ConfigurationException e)
      {
         throw new ProcessingException(e);
      }
      catch (final ParsingException e)
      {
         throw new ProcessingException(e);
      }
   }

   public static String assertionToString(final Element assertion)
   {
      if (assertion == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "assertion");

      try
      {
         final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

         final Source source = new DOMSource(assertion);
         final StringWriter writer = new StringWriter();
         final Result result = new StreamResult(writer);

         transformer.transform(source, result);

         return writer.toString();
      }
      catch (final TransformerException e)
      {
         throw new IllegalStateException(e.getMessage(), e);
      }
   }
}