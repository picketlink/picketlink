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

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.picketlink.identity.federation.bindings.tomcat.sp.holder.ServiceProviderSAMLContext;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.ServerDetector;

/**
 * Authenticator for SAML 1.1 processing at the Service Provider
 * @author Anil.Saldhana@redhat.com
 * @since Jul 7, 2011
 */
public class SAML11SPRedirectFormAuthenticator extends SPRedirectFormAuthenticator
{
   @Override
   public boolean authenticate(Request request, Response response, LoginConfig loginConfig) throws IOException
   {
      String samlResponse = request.getParameter(GeneralConstants.SAML_RESPONSE_KEY);

      Principal principal = request.getUserPrincipal();

      //If we have already authenticated the user and there is no request from IDP or logout from user
      if (principal != null)
         return true;

      Session session = request.getSessionInternal(true);

      //See if we got a response from IDP
      if (isNotNull(samlResponse))
      {
         boolean isValid = false;
         try
         {
            isValid = this.validate(request);
         }
         catch (Exception e)
         {
            log.error("Exception:", e);
            throw new IOException();
         }
         if (!isValid)
            throw new IOException(ErrorCodes.VALIDATION_CHECK_FAILED);

         try
         {
            InputStream base64DecodedResponse = RedirectBindingUtil.base64DeflateDecode(samlResponse);
            SAMLParser parser = new SAMLParser();
            SAML11ResponseType saml11Response = (SAML11ResponseType) parser.parse(base64DecodedResponse);

            List<SAML11AssertionType> assertions = saml11Response.get();
            if (assertions.size() > 1)
            {
               if (trace)
                  log.trace("More than one assertion from IDP. Considering the first one.");
            }
            String username = null;
            List<String> roles = new ArrayList<String>();
            SAML11AssertionType assertion = assertions.get(0);
            if (assertion != null)
            {
               //Get the subject
               List<SAML11StatementAbstractType> statements = assertion.getStatements();
               for (SAML11StatementAbstractType statement : statements)
               {
                  if (statement instanceof SAML11AuthenticationStatementType)
                  {
                     SAML11AuthenticationStatementType subStat = (SAML11AuthenticationStatementType) statement;
                     SAML11SubjectType subject = subStat.getSubject();
                     username = subject.getChoice().getNameID().getValue();
                  }
               }
               roles = AssertionUtil.getRoles(assertion, null);
            }

            String password = ServiceProviderSAMLContext.EMPTY_PASSWORD;

            //Map to JBoss specific principal
            if ((new ServerDetector()).isJboss() || jbossEnv)
            {
               //Push a context
               ServiceProviderSAMLContext.push(username, roles);
               principal = context.getRealm().authenticate(username, password);
               ServiceProviderSAMLContext.clear();
            }
            else
            {
               //tomcat env   
               SPUtil spUtil = new SPUtil();
               principal = spUtil.createGenericPrincipal(request, username, roles);
            }

            session.setNote(Constants.SESS_USERNAME_NOTE, username);
            session.setNote(Constants.SESS_PASSWORD_NOTE, password);
            request.setUserPrincipal(principal);

            if (saveRestoreRequest)
            {
               this.restoreRequest(request, session);
            }
            register(request, response, principal, Constants.FORM_METHOD, username, password);

            return true;
         }
         catch (Exception e)
         {
            log.error("Processing Exception:", e);
         }
      }

      log.error("Falling back on local Form Authentication if available");
      //fallback
      return super.authenticate(request, response, loginConfig);
   }
}