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

import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ActionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeDesignatorType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11EvidenceType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AttributeQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthenticationQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthorizationDecisionQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11QueryAbstractType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11RequestType;

/**
 * Write the {@link SAML11RequestType} to stream
 * @author Anil.Saldhana@redhat.com
 * @since Jun 27, 2011
 */
public class SAML11RequestWriter extends BaseSAML11Writer
{
   protected String namespace = SAML11Constants.PROTOCOL_11_NSURI;

   protected SAML11AssertionWriter assertionWriter;

   public SAML11RequestWriter(XMLStreamWriter writer)
   {
      super(writer);
      assertionWriter = new SAML11AssertionWriter(writer);
   }

   public void write(SAML11RequestType request) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.REQUEST, namespace);
      StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, namespace);
      StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, SAML11Constants.ASSERTION_11_NSURI);
      StaxUtil.writeDefaultNameSpace(writer, namespace);

      // Attributes
      StaxUtil.writeAttribute(writer, SAML11Constants.REQUEST_ID, request.getID());
      StaxUtil.writeAttribute(writer, SAML11Constants.MAJOR_VERSION, request.getMajorVersion() + "");
      StaxUtil.writeAttribute(writer, SAML11Constants.MINOR_VERSION, request.getMinorVersion() + "");
      StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), request.getIssueInstant().toString());

      List<String> assertionIDRefs = request.getAssertionIDRef();
      for (String assertionIDRef : assertionIDRefs)
      {
         StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, SAML11Constants.ASSERTION_ID_REF,
               SAML11Constants.ASSERTION_11_NSURI);
         StaxUtil.writeCharacters(writer, assertionIDRef);
         StaxUtil.writeEndElement(writer);
      }

      List<String> assertionArtifacts = request.getAssertionArtifact();
      for (String assertionArtifact : assertionArtifacts)
      {
         StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.ASSERTION_ARTIFACT, namespace);
         StaxUtil.writeCharacters(writer, assertionArtifact);
         StaxUtil.writeEndElement(writer);
      }

      SAML11QueryAbstractType query = request.getQuery();
      if (query instanceof SAML11AuthenticationQueryType)
      {
         SAML11AuthenticationQueryType authQuery = (SAML11AuthenticationQueryType) query;
         write(authQuery);
      }
      else if (query instanceof SAML11AttributeQueryType)
      {
         SAML11AttributeQueryType attQuery = (SAML11AttributeQueryType) query;
         write(attQuery);
      }
      else if (query instanceof SAML11AuthenticationQueryType)
      {
         SAML11AuthenticationQueryType attQuery = (SAML11AuthenticationQueryType) query;
         write(attQuery);
      }
      else if (query instanceof SAML11AuthorizationDecisionQueryType)
      {
         SAML11AuthorizationDecisionQueryType attQuery = (SAML11AuthorizationDecisionQueryType) query;
         write(attQuery);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(SAML11AuthenticationQueryType auth) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.AUTHENTICATION_QUERY, namespace);

      URI authMethod = auth.getAuthenticationMethod();
      if (authMethod != null)
      {
         StaxUtil.writeAttribute(writer, SAML11Constants.AUTHENTICATION_METHOD, authMethod.toString());
      }

      SAML11SubjectType subject = auth.getSubject();
      if (subject != null)
      {
         assertionWriter.write(subject);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(SAML11AttributeQueryType attr) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.ATTRIBUTE_QUERY, namespace);

      URI resource = attr.getResource();
      if (resource != null)
      {
         StaxUtil.writeAttribute(writer, SAML11Constants.RESOURCE, resource.toString());
      }

      SAML11SubjectType subject = attr.getSubject();
      if (subject != null)
      {
         assertionWriter.write(subject);
      }

      List<SAML11AttributeDesignatorType> attributes = attr.get();
      for (SAML11AttributeDesignatorType attribute : attributes)
      {
         if (attribute instanceof SAML11AttributeType)
         {
            SAML11AttributeType sat = (SAML11AttributeType) attribute;
            assertionWriter.write(sat);
         }
         else
            throw new ProcessingException(ErrorCodes.WRITER_UNKNOWN_TYPE + attribute.getClass());
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(SAML11AuthorizationDecisionQueryType attr) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.AUTHORIZATION_DECISION_QUERY, namespace);

      URI resource = attr.getResource();
      if (resource != null)
      {
         StaxUtil.writeAttribute(writer, SAML11Constants.RESOURCE, resource.toString());
      }

      SAML11SubjectType subject = attr.getSubject();
      if (subject != null)
      {
         assertionWriter.write(subject);
      }

      List<SAML11ActionType> actions = attr.get();
      for (SAML11ActionType action : actions)
      {
         assertionWriter.write(action);
      }

      SAML11EvidenceType evidence = attr.getEvidence();
      if (evidence != null)
      {
         assertionWriter.write(evidence);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }
}