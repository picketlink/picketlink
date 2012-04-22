/*
 * JBoss, Home of Professional Open Source. Copyright 2009, Red Hat Middleware LLC, and individual contributors as
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
package org.picketlink.identity.federation.core.wstrust;

import java.net.URI;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.ClaimsType;
import org.picketlink.identity.federation.ws.trust.ComputedKeyType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.RequestedProofTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedSecurityTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedTokenCancelledType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * Default implementation of the {@code WSTrustRequestHandler} interface. It creates the request context containing the
 * original WS-Trust request as well as any information that may be relevant to the token processing, and delegates the
 * actual token handling processing to the appropriate {@code SecurityTokenProvider}.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class StandardRequestHandler implements WSTrustRequestHandler
{
   private static Logger log = Logger.getLogger(StandardRequestHandler.class);

   private final boolean trace = log.isTraceEnabled();

   private static long KEY_SIZE = 128;

   private STSConfiguration configuration;

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler#initialize(
    *   org.picketlink.identity.federation.core.wstrust.STSConfiguration)
    */
   public void initialize(STSConfiguration configuration)
   {
      this.configuration = configuration;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler#issue(
    *   org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken, java.security.Principal)
    */
   public RequestSecurityTokenResponse issue(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException
   {
      if (trace)
         log.trace("Issuing token for principal " + callerPrincipal);

      //SecurityTokenProvider provider = null;

      // first try to obtain the security token provider using the applies-to contents.
      AppliesTo appliesTo = request.getAppliesTo();
      PublicKey providerPublicKey = null;
      if (appliesTo != null)
      {
         String serviceName = WSTrustUtil.parseAppliesTo(appliesTo);

         if (serviceName != null)
         {
            String tokenTypeFromServiceName = configuration.getTokenTypeForService(serviceName);

            if (request.getTokenType() == null && tokenTypeFromServiceName != null)
               request.setTokenType(URI.create(tokenTypeFromServiceName));

            providerPublicKey = this.configuration.getServiceProviderPublicKey(serviceName);

            // provider = this.configuration.getProviderForService(serviceName);
            /*if (provider != null)
            {
               request.setTokenType(URI.create(this.configuration.getTokenTypeForService(serviceName)));
               providerPublicKey = this.configuration.getServiceProviderPublicKey(serviceName);
            }*/
         }
      }
      // if applies-to is not available or if no provider was found for the service, use the token type.
      /*if (provider == null && request.getTokenType() != null)
      { 
         provider = this.configuration.getProviderForTokenType(request.getTokenType().toString());
      }
      else if (appliesTo == null && request.getTokenType() == null)
         throw new WSTrustException("Either AppliesTo or TokenType must be present in a security token request");

      if (provider != null)
      {*/
      // create the request context and delegate token generation to the provider.
      WSTrustRequestContext requestContext = new WSTrustRequestContext(request, callerPrincipal);
      requestContext.setTokenIssuer(this.configuration.getSTSName());
      if (request.getLifetime() == null && this.configuration.getIssuedTokenTimeout() != 0)
      {
         // if no lifetime has been specified, use the configured timeout value.
         if (log.isDebugEnabled())
            log.debug("Lifetime has not been specified. Using the default timeout value.");
         request.setLifetime(WSTrustUtil.createDefaultLifetime(this.configuration.getIssuedTokenTimeout()));
      }
      requestContext.setServiceProviderPublicKey(providerPublicKey);

      // process the claims if needed.
      if (request.getClaims() != null)
      {
         ClaimsType claims = request.getClaims();
         ClaimsProcessor processor = this.configuration.getClaimsProcessor(claims.getDialect());
         // if there is a processor, process the claims and set the resulting attributes in the context.
         if (processor != null)
            requestContext.setClaimedAttributes(processor.processClaims(claims, callerPrincipal));
         else if (log.isDebugEnabled())
            log.debug("Claims have been specified in the request but no processor was found for dialect "
                  + claims.getDialect());
      }

      // get the OnBehalfOf principal, if one has been specified.
      if (request.getOnBehalfOf() != null)
      {
         Principal onBehalfOfPrincipal = WSTrustUtil.getOnBehalfOfPrincipal(request.getOnBehalfOf());
         requestContext.setOnBehalfOfPrincipal(onBehalfOfPrincipal);
      }

      // get the key type and size from the request, setting default values if not specified.
      URI keyType = request.getKeyType();
      if (keyType == null)
      {
         if (log.isDebugEnabled())
            log.debug("No key type could be found in the request. Using the default BEARER type.");
         keyType = URI.create(WSTrustConstants.KEY_TYPE_BEARER);
         request.setKeyType(keyType);
      }
      long keySize = request.getKeySize();
      if (keySize == 0)
      {
         if (log.isDebugEnabled())
            log.debug("No key size could be found in the request. Using the default size. (" + KEY_SIZE + ")");
         keySize = KEY_SIZE;
         request.setKeySize(keySize);
      }

      // get the key wrap algorithm.
      URI keyWrapAlgo = request.getKeyWrapAlgorithm();

      // create proof-of-possession token and server entropy (if needed).
      RequestedProofTokenType requestedProofToken = null;
      EntropyType serverEntropy = null;

      if (WSTrustConstants.KEY_TYPE_SYMMETRIC.equalsIgnoreCase(keyType.toString()))
      {
         // symmetric key case: if client entropy is found, compute a key. If not, generate a new key.
         requestedProofToken = new RequestedProofTokenType();

         byte[] serverSecret = WSTrustUtil.createRandomSecret((int) keySize / 8);
         BinarySecretType serverBinarySecret = new BinarySecretType();
         serverBinarySecret.setType(WSTrustConstants.BS_TYPE_NONCE);
         serverBinarySecret.setValue(Base64.encodeBytes(serverSecret).getBytes());

         byte[] clientSecret = null;
         EntropyType clientEntropy = request.getEntropy();
         if (clientEntropy != null)
         {
            clientSecret = Base64.decode(new String(WSTrustUtil.getBinarySecret(clientEntropy)));
            serverEntropy = new EntropyType();
            serverEntropy.addAny(serverBinarySecret);
         }

         if (clientSecret != null && clientSecret.length != 0)
         {
            // client secret has been specified - combine it with the sts secret.
            requestedProofToken.add(new ComputedKeyType(WSTrustConstants.CK_PSHA1));
            byte[] combinedSecret = null;
            try
            {
               combinedSecret = Base64.encodeBytes(WSTrustUtil.P_SHA1(clientSecret, serverSecret, (int) keySize / 8))
                     .getBytes();
            }
            catch (Exception e)
            {
               throw new WSTrustException(ErrorCodes.STS_COMBINED_SECRET_KEY_ERROR, e);
            }
            requestContext.setProofTokenInfo(WSTrustUtil.createKeyInfo(combinedSecret, providerPublicKey, keyWrapAlgo));
         }
         else
         {
            // client secret has not been specified - use the sts secret only.
            requestedProofToken.add(serverBinarySecret);
            requestContext.setProofTokenInfo(WSTrustUtil.createKeyInfo(serverBinarySecret.getValue(),
                  providerPublicKey, keyWrapAlgo));
         }
      }
      else if (WSTrustConstants.KEY_TYPE_PUBLIC.equalsIgnoreCase(keyType.toString()))
      {
         // try to locate the client cert in the keystore using the caller principal as the alias.
         Certificate certificate = this.configuration.getCertificate(callerPrincipal.getName());
         if (certificate != null)
            requestContext.setProofTokenInfo(WSTrustUtil.createKeyInfo(certificate));
         // if no certificate was found in the keystore, check the UseKey contents.
         else if (request.getUseKey() != null)
         {
            UseKeyType useKeyType = request.getUseKey();
            List<Object> theList = useKeyType.getAny();
            for (Object value : theList)
            {
               if (value instanceof Element)
               {
                  String elementName = ((Element) value).getLocalName();
                  // if the specified key is a X509 certificate we must insert it into a X509Data element.
                  if (elementName.equals("X509Certificate"))
                  {
                     X509DataType data = new X509DataType();
                     data.add(value);
                     value = data;
                  }
                  KeyInfoType keyInfo = new KeyInfoType();
                  keyInfo.addContent(value);
                  requestContext.setProofTokenInfo(keyInfo);
               }
            }
            /*Object value = useKeyType.getAny();
            if (value instanceof Element)
            {
               String elementName = ((Element) value).getLocalName();
               // if the specified key is a X509 certificate we must insert it into a X509Data element.
               if (elementName.equals("X509Certificate"))
               {
                  X509DataType data = new X509DataType();
                  data.add(value);
                  value = data;
               }
               KeyInfoType keyInfo = new KeyInfoType();
               keyInfo.addContent(value);
               requestContext.setProofTokenInfo(keyInfo);
            }*/
         }
         else
            throw new WSTrustException(ErrorCodes.STS_CLIENT_PUBLIC_KEY_ERROR);
      }

      // issue the security token using the constructed context.
      try
      {
         if (request.getTokenType() != null)
            requestContext.setTokenType(request.getTokenType().toString());
         PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
         sts.initialize(configuration);
         sts.issueToken(requestContext);
         //provider.issueToken(requestContext);
      }
      catch (ProcessingException e)
      {
         throw new WSTrustException(e.getMessage(), e);
      }

      if (requestContext.getSecurityToken() == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "Token issued by STS");

      // construct the ws-trust security token response.
      RequestedSecurityTokenType requestedSecurityToken = new RequestedSecurityTokenType();

      SecurityToken contextSecurityToken = requestContext.getSecurityToken();
      if (contextSecurityToken == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "Security Token from context");

      requestedSecurityToken.add(contextSecurityToken.getTokenValue());

      RequestSecurityTokenResponse response = new RequestSecurityTokenResponse();
      if (request.getContext() != null)
         response.setContext(request.getContext());

      response.setTokenType(request.getTokenType());
      response.setLifetime(request.getLifetime());
      response.setAppliesTo(appliesTo);
      response.setKeySize(keySize);
      response.setKeyType(keyType);
      response.setRequestedSecurityToken(requestedSecurityToken);

      if (requestedProofToken != null)
         response.setRequestedProofToken(requestedProofToken);
      if (serverEntropy != null)
         response.setEntropy(serverEntropy);

      // set the attached and unattached references.
      if (requestContext.getAttachedReference() != null)
         response.setRequestedAttachedReference(requestContext.getAttachedReference());
      if (requestContext.getUnattachedReference() != null)
         response.setRequestedUnattachedReference(requestContext.getUnattachedReference());

      return response;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler#renew(
    *   org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken, java.security.Principal)
    */
   public RequestSecurityTokenResponse renew(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException
   {
      // first validate the provided token signature to make sure it has been issued by this STS and hasn't been
      // tempered.
      if (trace)
         log.trace("Validating token for renew request " + request.getContext());
      if (request.getRenewTargetElement() == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE
               + "Unable to renew token: request does not have a renew target");

      Node securityToken = request.getRenewTargetElement().getFirstChild();
      if (securityToken == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "Unable to renew token: security token is null");

      /*SecurityTokenProvider provider = this.configuration.getProviderForTokenElementNS(securityToken.getLocalName(),
            securityToken.getNamespaceURI());
      if (provider == null)
         throw new WSTrustException("No SecurityTokenProvider configured for " + securityToken.getNamespaceURI() + ":"
               + securityToken.getLocalName());*/

      if (this.configuration.signIssuedToken() && this.configuration.getSTSKeyPair() != null)
      {
         KeyPair keyPair = this.configuration.getSTSKeyPair();
         try
         {
            Document tokenDocument = DocumentUtil.createDocument();
            Node importedNode = tokenDocument.importNode(securityToken, true);
            tokenDocument.appendChild(importedNode);
            if (!XMLSignatureUtil.validate(tokenDocument, keyPair.getPublic()))
               throw new WSTrustException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Validation failure during renewal");
         }
         catch (Exception e)
         {
            throw new WSTrustException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Validation failure during renewal:", e);
         }
      }
      else
      {
         if (trace)
            log.trace("Security Token digital signature has NOT been verified. Either the STS has been configured"
                  + "not to sign tokens or the STS key pair has not been properly specified.");
      }

      // set default values where needed.
      if (request.getLifetime() == null && this.configuration.getIssuedTokenTimeout() != 0)
      {
         // if no lifetime has been specified, use the configured timeout value.
         if (log.isDebugEnabled())
            log.debug("Lifetime has not been specified. Using the default timeout value.");
         request.setLifetime(WSTrustUtil.createDefaultLifetime(this.configuration.getIssuedTokenTimeout()));
      }

      // create a context and dispatch to the proper security token provider for renewal.
      WSTrustRequestContext context = new WSTrustRequestContext(request, callerPrincipal);
      context.setTokenIssuer(this.configuration.getSTSName());
      // if the renew request was made on behalf of another identity, get the principal of that identity.
      if (request.getOnBehalfOf() != null)
      {
         Principal onBehalfOfPrincipal = WSTrustUtil.getOnBehalfOfPrincipal(request.getOnBehalfOf());
         context.setOnBehalfOfPrincipal(onBehalfOfPrincipal);
      }
      try
      {
         if (securityToken != null)
         {
            String ns = securityToken.getNamespaceURI();

            context.setQName(new QName(ns, securityToken.getLocalName()));
         }
         PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
         sts.initialize(configuration);
         sts.renewToken(context);
         //provider.renewToken(context);
      }
      catch (ProcessingException e)
      {
         throw new WSTrustException(e.getMessage(), e);
      }

      // create the WS-Trust response with the renewed token.
      RequestedSecurityTokenType requestedSecurityToken = new RequestedSecurityTokenType();
      SecurityToken contextSecurityToken = context.getSecurityToken();
      if (contextSecurityToken == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "Security Token from context");
      requestedSecurityToken.add(contextSecurityToken.getTokenValue());

      RequestSecurityTokenResponse response = new RequestSecurityTokenResponse();
      if (request.getContext() != null)
         response.setContext(request.getContext());
      response.setTokenType(request.getTokenType());
      response.setLifetime(request.getLifetime());
      response.setRequestedSecurityToken(requestedSecurityToken);
      if (context.getAttachedReference() != null)
         response.setRequestedAttachedReference(context.getAttachedReference());
      if (context.getUnattachedReference() != null)
         response.setRequestedUnattachedReference(context.getUnattachedReference());
      return response;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler#validate(
    *   org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken, java.security.Principal)
    */
   public RequestSecurityTokenResponse validate(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException
   {
      if (trace)
         log.trace("Started validation for request " + request.getContext());

      if (request.getValidateTargetElement() == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE
               + "request does not have a validate target. Unable to validate token");

      if (request.getTokenType() == null)
         request.setTokenType(URI.create(WSTrustConstants.STATUS_TYPE));

      Node securityToken = request.getValidateTargetElement().getFirstChild();
      if (securityToken == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "security token:Unable to validate token");

      WSTrustRequestContext context = new WSTrustRequestContext(request, callerPrincipal);
      // if the validate request was made on behalf of another identity, get the principal of that identity.
      if (request.getOnBehalfOf() != null)
      {
         Principal onBehalfOfPrincipal = WSTrustUtil.getOnBehalfOfPrincipal(request.getOnBehalfOf());
         context.setOnBehalfOfPrincipal(onBehalfOfPrincipal);
      }
      StatusType status = null;

      // validate the security token digital signature.
      if (this.configuration.signIssuedToken() && this.configuration.getSTSKeyPair() != null)
      {
         KeyPair keyPair = this.configuration.getSTSKeyPair();
         try
         {
            if (trace)
            {
               try
               {
                  log.trace("Going to validate:" + DocumentUtil.getNodeAsString(securityToken));
               }
               catch (Exception e)
               {
               }
            }
            Document tokenDocument = DocumentUtil.createDocument();
            Node importedNode = tokenDocument.importNode(securityToken, true);
            tokenDocument.appendChild(importedNode);
            if (!XMLSignatureUtil.validate(tokenDocument, keyPair.getPublic()))
            {
               status = new StatusType();
               status.setCode(WSTrustConstants.STATUS_CODE_INVALID);
               status.setReason("Validation failure: digital signature is invalid");
            }
         }
         catch (Exception e)
         {
            status = new StatusType();
            status.setCode(WSTrustConstants.STATUS_CODE_INVALID);
            status.setReason("Validation failure: unable to verify digital signature: " + e.getMessage());
         }
      }
      else
      {
         if (trace)
            log.trace("Security Token digital signature has NOT been verified. Either the STS has been configured"
                  + "not to sign tokens or the STS key pair has not been properly specified.");
      }

      // if the signature is valid, then let the provider perform any additional validation checks.
      if (status == null)
      {
         if (trace)
            log.trace("Delegating token validation to token provider");
         try
         {
            if (securityToken != null)
               context.setQName(new QName(securityToken.getNamespaceURI(), securityToken.getLocalName()));
            PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
            sts.initialize(configuration);
            sts.validateToken(context);
            //provider.validateToken(context);
         }
         catch (ProcessingException e)
         {
            throw new WSTrustException(e.getMessage(), e);
         }
         status = context.getStatus();
      }

      // construct and return the response.
      RequestSecurityTokenResponse response = new RequestSecurityTokenResponse();
      if (request.getContext() != null)
         response.setContext(request.getContext());
      response.setTokenType(request.getTokenType());
      response.setStatus(status);

      return response;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler#cancel(
    *   org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken, java.security.Principal)
    */
   public RequestSecurityTokenResponse cancel(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException
   {
      // check if request contains all required elements.
      if (request.getCancelTargetElement() == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE
               + "request does not have a cancel target. Unable to cancel token");

      // obtain the token provider that will handle the request.
      Node securityToken = request.getCancelTargetElement().getFirstChild();
      if (securityToken == null)
         throw new WSTrustException(ErrorCodes.NULL_VALUE + "security token. Unable to cancel token");

      /*SecurityTokenProvider provider = this.configuration.getProviderForTokenElementNS(securityToken.getLocalName(),
            securityToken.getNamespaceURI());
      if (provider == null)
         throw new WSTrustException("No SecurityTokenProvider configured for " + securityToken.getNamespaceURI() + ":"
               + securityToken.getLocalName());*/

      // create a request context and dispatch to the provider.
      WSTrustRequestContext context = new WSTrustRequestContext(request, callerPrincipal);
      // if the cancel request was made on behalf of another identity, get the principal of that identity.
      if (request.getOnBehalfOf() != null)
      {
         Principal onBehalfOfPrincipal = WSTrustUtil.getOnBehalfOfPrincipal(request.getOnBehalfOf());
         context.setOnBehalfOfPrincipal(onBehalfOfPrincipal);
      }
      try
      {
         if (securityToken != null)
            context.setQName(new QName(securityToken.getNamespaceURI(), securityToken.getLocalName()));
         PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
         sts.initialize(configuration);
         sts.cancelToken(context);
         //provider.cancelToken(context);
      }
      catch (ProcessingException e)
      {
         throw new WSTrustException(e.getMessage(), e);
      }

      // if no exception has been raised, the token has been successfully canceled.
      RequestSecurityTokenResponse response = new RequestSecurityTokenResponse();
      if (request.getContext() != null)
         response.setContext(request.getContext());
      response.setRequestedTokenCancelled(new RequestedTokenCancelledType());
      return response;
   }

   public Document postProcess(Document rstrDocument, RequestSecurityToken request) throws WSTrustException
   {
      if (WSTrustConstants.ISSUE_REQUEST.equals(request.getRequestType().toString())
            || WSTrustConstants.RENEW_REQUEST.equals(request.getRequestType().toString()))
      {
         rstrDocument = DocumentUtil.normalizeNamespaces(rstrDocument);

         // Sign the security token
         if (this.configuration.signIssuedToken() && this.configuration.getSTSKeyPair() != null)
         {
            KeyPair keyPair = this.configuration.getSTSKeyPair();
            URI signatureURI = request.getSignatureAlgorithm();
            String signatureMethod = signatureURI != null ? signatureURI.toString() : SignatureMethod.RSA_SHA1;
            try
            {
               Node rst = rstrDocument
                     .getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken").item(0);
               Element tokenElement = (Element) rst.getFirstChild();
               if (trace)
                  log.trace("NamespaceURI of element to be signed:" + tokenElement.getNamespaceURI());

               // Set the CanonicalizationMethod if any
               XMLSignatureUtil.setCanonicalizationMethodType(configuration.getXMLDSigCanonicalizationMethod());

               /*rstrDocument = XMLSignatureUtil.sign(rstrDocument, tokenElement, keyPair, DigestMethod.SHA1,
                     signatureMethod, "#" + tokenElement.getAttribute("ID"));*/
               rstrDocument = XMLSignatureUtil.sign(rstrDocument, tokenElement, keyPair, DigestMethod.SHA1,
                     signatureMethod, "");
               if (trace)
               {
                  try
                  {
                     log.trace("Signed Token:" + DocumentUtil.getNodeAsString(tokenElement));

                     Document tokenDocument = DocumentUtil.createDocument();
                     tokenDocument.appendChild(tokenDocument.importNode(tokenElement, true));
                     log.trace("valid=" + XMLSignatureUtil.validate(tokenDocument, keyPair.getPublic()));

                  }
                  catch (Exception ignore)
                  {
                  }
               }
            }
            catch (Exception e)
            {
               throw new WSTrustException(ErrorCodes.SIGNING_PROCESS_FAILURE, e);
            }
         }

         // encrypt the security token if needed.
         if (this.configuration.encryptIssuedToken())
         {
            // get the public key that will be used to encrypt the token.
            PublicKey providerPublicKey = null;
            if (request.getAppliesTo() != null)
            {
               String serviceName = WSTrustUtil.parseAppliesTo(request.getAppliesTo());
               if (trace)
                  log.trace("Locating public key for service provider " + serviceName);
               if (serviceName != null)
                  providerPublicKey = this.configuration.getServiceProviderPublicKey(serviceName);
            }
            if (providerPublicKey == null)
            {
               log.warn("Security token should be encrypted but no encrypting key could be found");
            }
            else
            {
               // generate the secret key.
               long keySize = request.getKeySize();
               byte[] secret = WSTrustUtil.createRandomSecret((int) keySize / 8);
               SecretKey secretKey = new SecretKeySpec(secret, "AES");

               // encrypt the security token.
               Node rst = rstrDocument
                     .getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken").item(0);
               Element tokenElement = (Element) rst.getFirstChild();
               try
               {
                  XMLEncryptionUtil.encryptElement(rstrDocument, tokenElement, providerPublicKey, secretKey,
                        (int) keySize);
               }
               catch (ProcessingException e)
               {
                  throw new WSTrustException(ErrorCodes.ENCRYPTION_PROCESS_FAILURE, e);
               }
            }
         }
      }

      return rstrDocument;
   }

}