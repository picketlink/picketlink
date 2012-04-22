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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLEventReader;

import org.jboss.security.xacml.core.JBossRequestContext;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.jboss.security.xacml.interfaces.RequestContext;
import org.jboss.security.xacml.interfaces.ResponseContext;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.factories.XACMLContextFactory;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.parsers.saml.xacml.SAMLXACMLRequestParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Node;

/**
 * Utility associated with SOAP 1.1 Envelope,
 * SAML2 and XACML2
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
public class SOAPSAMLXACMLUtil
{
   /**
    * Parse the XACML Authorization Decision Query from the Dom Element
    * @param samlRequest
    * @return 
    * @throws ProcessingException 
    * @throws ConfigurationException  
    * @throws ParsingException
    */
   public static XACMLAuthzDecisionQueryType getXACMLQueryType(Node samlRequest) throws ParsingException,
         ConfigurationException, ProcessingException
   {
      //We reparse it because the document may have issues with namespaces
      //String elementString = DocumentUtil.getDOMElementAsString(samlRequest);

      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(samlRequest));
      SAMLXACMLRequestParser samlXACMLRequestParser = new SAMLXACMLRequestParser();
      return (XACMLAuthzDecisionQueryType) samlXACMLRequestParser.parse(xmlEventReader);

      /*Unmarshaller um = JAXBUtil.getUnmarshaller(collectivePackage);
      um.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

      JAXBElement<?> obj = (JAXBElement<?>) um.unmarshal(new StringReader(elementString));
      Object xacmlObject = obj.getValue();
      if(xacmlObject instanceof XACMLAuthzDecisionQueryType == false)
         throw new RuntimeException("Unsupported type:" + xacmlObject);
      return (XACMLAuthzDecisionQueryType)xacmlObject;  */
   }

   public static XACMLAuthzDecisionStatementType getDecisionStatement(Node samlResponse) throws ConfigurationException,
         ProcessingException, ParsingException
   {
      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(samlResponse));
      SAMLParser samlParser = new SAMLParser();

      JAXPValidationUtil.checkSchemaValidation(samlResponse);

      org.picketlink.identity.federation.saml.v2.protocol.ResponseType response = (org.picketlink.identity.federation.saml.v2.protocol.ResponseType) samlParser
            .parse(xmlEventReader);
      List<RTChoiceType> choices = response.getAssertions();
      for (RTChoiceType rst : choices)
      {
         AssertionType assertion = rst.getAssertion();
         if (assertion == null)
            continue;
         Set<StatementAbstractType> stats = assertion.getStatements();
         for (StatementAbstractType stat : stats)
         {
            if (stat instanceof XACMLAuthzDecisionStatementType)
            {
               return (XACMLAuthzDecisionStatementType) stat;
            }
         }
      }

      throw new RuntimeException("Not found XACMLAuthzDecisionStatementType");
   }

   public synchronized static org.picketlink.identity.federation.saml.v2.protocol.ResponseType handleXACMLQuery(
         PolicyDecisionPoint pdp, String issuer, XACMLAuthzDecisionQueryType xacmlRequest) throws ProcessingException,
         ConfigurationException
   {
      RequestType requestType = xacmlRequest.getRequest();

      RequestContext requestContext = new JBossRequestContext();
      try
      {
         requestContext.setRequest(requestType);
      }
      catch (IOException e)
      {
         throw new ProcessingException(e);
      }

      //pdp evaluation is thread safe
      ResponseContext responseContext = pdp.evaluate(requestContext);

      ResponseType responseType = new ResponseType();
      ResultType resultType = responseContext.getResult();
      responseType.getResult().add(resultType);

      XACMLAuthzDecisionStatementType xacmlStatement = XACMLContextFactory.createXACMLAuthzDecisionStatementType(
            requestType, responseType);

      //Place the xacml statement in an assertion
      //Then the assertion goes inside a SAML Response

      String ID = IDGenerator.create("ID_");
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder(issuer);

      List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
      statements.add(xacmlStatement);

      AssertionType assertion = SAMLAssertionFactory.createAssertion(ID, issuerInfo.getIssuer(),
            XMLTimeUtil.getIssueInstant(), null, null, statements);

      org.picketlink.identity.federation.saml.v2.protocol.ResponseType samlResponseType = JBossSAMLAuthnResponseFactory
            .createResponseType(ID, issuerInfo, assertion);

      return samlResponseType;
   }
}