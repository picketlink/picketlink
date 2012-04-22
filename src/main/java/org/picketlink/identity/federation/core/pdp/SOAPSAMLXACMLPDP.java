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
package org.picketlink.identity.federation.core.pdp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedActionException;

import javax.annotation.Resource;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

import org.apache.log4j.Logger;
import org.jboss.security.xacml.core.JBossPDP;
import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.SystemPropertiesUtil;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;

/**
 * SOAP 1.2 based XACML PDP that accepts SAML requests
 * @author Anil.Saldhana@redhat.com
 * @since Jan 24, 2011
 */
@WebServiceProvider(serviceName = "SOAPSAMLXACMLPDP", portName = "SOAPSAMLXACMLPort", targetNamespace = "urn:picketlink:identity-federation:pdp", wsdlLocation = "WEB-INF/wsdl/SOAPSAMLXACMLPDP.wsdl")
public class SOAPSAMLXACMLPDP implements Provider<Source>
{
   protected Logger log = Logger.getLogger(SOAPSAMLXACMLPDP.class);

   @Resource
   protected WebServiceContext context;

   protected String policyConfigFileName = "policyConfig.xml";

   protected PolicyDecisionPoint pdp;

   protected String issuer = "PicketLinkPDP";

   public SOAPSAMLXACMLPDP()
   {
      try
      {
         pdp = getPDP();
      }
      catch (PrivilegedActionException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Source invoke(Source request)
   {
      try
      {
         Document doc = (Document) DocumentUtil.getNodeFromSource(request);
         if (log.isDebugEnabled())
         {
            log.debug("Received Message::" + DocumentUtil.asString(doc));
         }
         XACMLAuthzDecisionQueryType xacmlQuery = SOAPSAMLXACMLUtil.getXACMLQueryType(doc);
         ResponseType samlResponseType = SOAPSAMLXACMLUtil.handleXACMLQuery(pdp, issuer, xacmlQuery);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         XMLStreamWriter xmlStreamWriter = StaxUtil.getXMLStreamWriter(baos);

         SAMLResponseWriter samlResponseWriter = new SAMLResponseWriter(xmlStreamWriter);
         samlResponseWriter.write(samlResponseType);
         Document responseDocument = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));

         return new DOMSource(responseDocument.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private PolicyDecisionPoint getPDP() throws PrivilegedActionException
   {
      SystemPropertiesUtil.ensure();

      URL url = SecurityActions.loadResource(getClass(), policyConfigFileName);
      if (url == null)
         throw new IllegalStateException(ErrorCodes.FILE_NOT_LOCATED + policyConfigFileName);

      InputStream is;
      try
      {
         is = url.openStream();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      return new JBossPDP(is);
   }
}