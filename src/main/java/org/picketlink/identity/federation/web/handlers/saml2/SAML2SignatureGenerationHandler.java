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
package org.picketlink.identity.federation.web.handlers.saml2;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.w3c.dom.Document;

/**
 * Handles SAML2 Signature
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SignatureGenerationHandler extends BaseSAML2Handler
{
   private static Logger log = Logger.getLogger(SAML2SignatureGenerationHandler.class);

   private final boolean trace = log.isTraceEnabled();

   @Override
   public void generateSAMLRequest(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   {
      //Generate the signature
      Document samlDocument = response.getResultingDocument();

      if (samlDocument == null && trace)
      {
         log.trace("No document generated in the handler chain. Cannot generate signature");
         return;
      }

      //Get the Key Pair
      KeyPair keypair = (KeyPair) this.handlerChainConfig.getParameter(GeneralConstants.KEYPAIR);

      if (keypair == null)
      {
         log.error("Key Pair cannot be found");
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "KeyPair not found");
      }

      sign(samlDocument, keypair);
   }

   public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException
   {
      Document responseDocument = response.getResultingDocument();
      if (responseDocument == null)
      {
         if (trace)
         {
            log.trace("handleRequestType:No response document found");
         }
         return;
      }

      //Get the Key Pair
      KeyPair keypair = (KeyPair) this.handlerChainConfig.getParameter(GeneralConstants.KEYPAIR);

      this.sign(responseDocument, keypair);
   }

   @Override
   public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response)
         throws ProcessingException
   {
      Document responseDocument = response.getResultingDocument();
      if (responseDocument == null)
      {
         if (trace)
         {
            log.trace("handleStatusResponseType:No response document found");
         }
         return;
      }

      //Get the Key Pair
      KeyPair keypair = (KeyPair) this.handlerChainConfig.getParameter(GeneralConstants.KEYPAIR);
      this.sign(responseDocument, keypair);
   }

   private void sign(Document samlDocument, KeyPair keypair) throws ProcessingException
   {
      SAML2Signature samlSignature = new SAML2Signature();
      samlSignature.signSAMLDocument(samlDocument, keypair);
   }
}