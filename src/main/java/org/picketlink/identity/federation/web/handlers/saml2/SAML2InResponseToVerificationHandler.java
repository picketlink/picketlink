/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.picketlink.identity.federation.web.handlers.saml2;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * Handler is useful on SP side. It's used for verification that InResponseId from SAML Authentication Response is same
 * as ID of previously sent SAML Authentication request
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SAML2InResponseToVerificationHandler extends BaseSAML2Handler
{
   private static Logger log = Logger.getLogger(SAML2InResponseToVerificationHandler.class);

   private final boolean trace = log.isTraceEnabled();

   @Override
   public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   {
      if (SAML2HandlerRequest.GENERATE_REQUEST_TYPE.AUTH != request.getTypeOfRequestToBeGenerated())
         return;

      if (getType() == HANDLER_TYPE.IDP)
         return;

      // Determine Id of of request, which is saved into session thanks to SAML2AuthenticationHandler
      String authnRequestId = (String) request.getOptions().get(GeneralConstants.AUTH_REQUEST_ID);

      // Save it into session for later use
      HttpSession session = BaseSAML2Handler.getHttpSession(request);
      session.setAttribute(GeneralConstants.AUTH_REQUEST_ID, authnRequestId);

      if (trace)
      {
         log.trace("ID of authentication request " + authnRequestId + " saved into HTTP session.");
      }
   }

   public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException
   {
   }

   @Override
   public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   {
      if (request.getSAML2Object() instanceof ResponseType == false)
         return;

      if (getType() == HANDLER_TYPE.IDP)
         return;

      // Obtain inResponseTo ID from Authentication response      
      ResponseType responseType = (ResponseType) request.getSAML2Object();
      String inResponseTo = responseType.getInResponseTo();

      // Obtain ID from session, which was saved before sending AuthnRequest
      HttpSession session = BaseSAML2Handler.getHttpSession(request);
      String authnRequestId = (String) session.getAttribute(GeneralConstants.AUTH_REQUEST_ID);

      // Remove it from session now
      session.removeAttribute(GeneralConstants.AUTH_REQUEST_ID);

      // Compare both ID
      if (inResponseTo != null && inResponseTo.equals(authnRequestId))
      {
         if (trace)
         {
            log.trace("Successful verification of InResponseTo for request " + inResponseTo);
         }
      }
      else
      {
         log.error("Verification of InResponseTo failed. InResponseTo from SAML response is " + inResponseTo
               + ". Value of request Id from HTTP session is " + authnRequestId);
         throw new ProcessingException(ErrorCodes.AUTHN_REQUEST_ID_VERIFICATION_FAILED);
      }
   }
}
