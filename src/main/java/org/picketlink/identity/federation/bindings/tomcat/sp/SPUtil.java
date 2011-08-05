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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.StatementLocal;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;

/**
 * Common code useful for a SP
 * @author Anil.Saldhana@redhat.com
 * @since Jan 9, 2009
 */
public class SPUtil
{
   /**
    * Create a SAML2 auth request
    * @param serviceURL URL of the service
    * @param identityURL URL of the identity provider
    * @return   
    * @throws ConfigurationException 
    */
   public AuthnRequestType createSAMLRequest(String serviceURL, String identityURL) throws ConfigurationException
   {
      if (serviceURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "serviceURL");
      if (identityURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "identityURL");

      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      return saml2Request.createAuthnRequestType(id, serviceURL, identityURL, serviceURL);
   }

   /**
    * Handle the SAMLResponse from the IDP
    * @param request entire request from IDP
    * @param responseType ResponseType that has been generated
    * @param serverEnvironment tomcat,jboss etc
    * @return   
    * @throws AssertionExpiredException 
    */
   public Principal handleSAMLResponse(Request request, ResponseType responseType) throws ConfigurationException,
         AssertionExpiredException
   {
      if (request == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "request");
      if (responseType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "response type");

      StatusType statusType = responseType.getStatus();
      if (statusType == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Status Type from the IDP");

      String statusValue = statusType.getStatusCode().getValue().toASCIIString();
      if (JBossSAMLURIConstants.STATUS_SUCCESS.get().equals(statusValue) == false)
         throw new SecurityException(ErrorCodes.IDP_AUTH_FAILED);

      List<RTChoiceType> assertions = responseType.getAssertions();
      if (assertions.size() == 0)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "No assertions in reply from IDP");

      AssertionType assertion = assertions.get(0).getAssertion();
      //Check for validity of assertion
      boolean expiredAssertion = AssertionUtil.hasExpired(assertion);
      if (expiredAssertion)
         throw new AssertionExpiredException();

      SubjectType subject = assertion.getSubject();

      //JAXBElement<NameIDType> jnameID = (JAXBElement<NameIDType>) subject.getContent().get(0);
      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      String userName = nameID.getValue();
      List<String> roles = new ArrayList<String>();

      //Set it on a thread local for JBID integrators
      StatementLocal.statements.set(assertion.getStatements());

      //Let us get the roles
      AttributeStatementType attributeStatement = (AttributeStatementType) assertion.getStatements().iterator().next();
      List<ASTChoiceType> attList = attributeStatement.getAttributes();
      for (ASTChoiceType obj : attList)
      {
         AttributeType attr = obj.getAttribute();
         String roleName = (String) attr.getAttributeValue().get(0);
         roles.add(roleName);
      }
      return this.createGenericPrincipal(request, userName, roles);
   }

   public Principal createGenericPrincipal(Request request, String username, List<String> roles)
   {
      Context ctx = request.getContext();
      return new GenericPrincipal(ctx.getRealm(), username, null, roles);
   }
}