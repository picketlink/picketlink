package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.sts.AbstractSecurityTokenProvider;
import org.picketlink.identity.federation.core.wstrust.SecurityToken;
import org.picketlink.identity.federation.core.wstrust.StandardSecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AudienceRestrictionCondition;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionsType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.w3c.dom.Element;

public class SAML11TokenProvider extends AbstractSecurityTokenProvider
{
   protected static Logger logger = Logger.getLogger(SAML11TokenProvider.class);

   /*
    * (non-Javadoc)
    * 
    * @seeorg.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#cancelToken(org.picketlink.identity.
    * federation.core.interfaces.ProtocolContext)
    */
   public void cancelToken(ProtocolContext context) throws ProcessingException
   {
      if (!(context instanceof WSTrustRequestContext))
         return;

      WSTrustRequestContext wstContext = (WSTrustRequestContext) context;

      // get the SAML assertion that will be canceled.
      Element token = wstContext.getRequestSecurityToken().getCancelTargetElement();
      if (token == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "Invalid cancel request: missing required CancelTarget");
      Element assertionElement = (Element) token.getFirstChild();
      if (!this.isSAMLAssertion(assertionElement))
         throw new ProcessingException(ErrorCodes.INVALID_ASSERTION
               + "CancelTarget doesn't not contain a SAMLV1.1 assertion");

      // get the assertion ID and add it to the canceled assertions set.
      String assertionId = assertionElement.getAttribute("AssertionID");
      this.revocationRegistry.revokeToken(SAMLUtil.SAML11_TOKEN_TYPE, assertionId);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#issueToken(org.picketlink.identity.federation
    * .core.interfaces.ProtocolContext)
    */
   public void issueToken(ProtocolContext context) throws ProcessingException
   {
      if (!(context instanceof WSTrustRequestContext))
         return;

      WSTrustRequestContext wstContext = (WSTrustRequestContext) context;
      // generate an id for the new assertion.
      String assertionID = IDGenerator.create("ID_");

      // lifetime and audience restrictions.
      Lifetime lifetime = wstContext.getRequestSecurityToken().getLifetime();
      SAML11AudienceRestrictionCondition restriction = null;
      AppliesTo appliesTo = wstContext.getRequestSecurityToken().getAppliesTo();
      if (appliesTo != null)
      {
         restriction = new SAML11AudienceRestrictionCondition();
         restriction.add(URI.create(WSTrustUtil.parseAppliesTo(appliesTo)));
      }
      SAML11ConditionsType conditions = new SAML11ConditionsType();
      conditions.setNotBefore(lifetime.getCreated());
      conditions.setNotOnOrAfter(lifetime.getExpires());
      conditions.add(restriction);

      // the assertion principal (default is caller principal)
      Principal principal = wstContext.getCallerPrincipal();

      String confirmationMethod = null;
      KeyInfoType keyInfoType = null;
      // if there is a on-behalf-of principal, we have the sender vouches confirmation method.
      if (wstContext.getOnBehalfOfPrincipal() != null)
      {
         principal = wstContext.getOnBehalfOfPrincipal();
         confirmationMethod = SAMLUtil.SAML11_SENDER_VOUCHES_URI;
      }
      // if there is a proof-of-possession token in the context, we have the holder of key confirmation method.
      else if (wstContext.getProofTokenInfo() != null)
      {
         confirmationMethod = SAMLUtil.SAML11_HOLDER_OF_KEY_URI;
         keyInfoType = wstContext.getProofTokenInfo();
      }
      else
         confirmationMethod = SAMLUtil.SAML11_BEARER_URI;

      SAML11SubjectConfirmationType subjectConfirmation = new SAML11SubjectConfirmationType();
      subjectConfirmation.addConfirmationMethod(URI.create(confirmationMethod));
      // TODO: set the key info.
      if (keyInfoType != null)
         throw new IllegalStateException(ErrorCodes.NOT_IMPLEMENTED_YET);

      // create a subject using the caller principal or on-behalf-of principal.
      String subjectName = principal == null ? "ANONYMOUS" : principal.getName();
      SAML11NameIdentifierType nameId = new SAML11NameIdentifierType(subjectName);
      nameId.setFormat(URI.create(SAML11Constants.FORMAT_UNSPECIFIED));
      SAML11SubjectType subject = new SAML11SubjectType();
      subject.setChoice(new SAML11SubjectType.SAML11SubjectTypeChoice(nameId));
      subject.setSubjectConfirmation(subjectConfirmation);

      // add the subject to an auth statement.
      SAML11AuthenticationStatementType authStatement = new SAML11AuthenticationStatementType(
            URI.create("urn:picketlink:auth"), lifetime.getCreated());
      authStatement.setSubject(subject);

      // TODO: add attribute statements.

      // create the SAML assertion.
      SAML11AssertionType assertion = new SAML11AssertionType(assertionID, lifetime.getCreated());
      assertion.add(authStatement);
      assertion.setConditions(conditions);
      assertion.setIssuer(wstContext.getTokenIssuer());

      // convert the constructed assertion to element.
      Element assertionElement = null;
      try
      {
         assertionElement = SAMLUtil.toElement(assertion);
      }
      catch (Exception e)
      {
         throw new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Failed to marshall SAMLV1.1 assertion", e);
      }
      SecurityToken token = new StandardSecurityToken(wstContext.getRequestSecurityToken().getTokenType().toString(),
            assertionElement, assertionID);
      wstContext.setSecurityToken(token);

      // set the SAML assertion attached reference.
      KeyIdentifierType keyIdentifier = WSTrustUtil.createKeyIdentifier(SAMLUtil.SAML11_VALUE_TYPE, "#" + assertionID);
      Map<QName, String> attributes = new HashMap<QName, String>();
      attributes.put(new QName(WSTrustConstants.WSSE11_NS, "TokenType", WSTrustConstants.WSSE.PREFIX_11),
            SAMLUtil.SAML11_TOKEN_TYPE);
      RequestedReferenceType attachedReference = WSTrustUtil.createRequestedReference(keyIdentifier, attributes);
      wstContext.setAttachedReference(attachedReference);

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#renewToken(org.picketlink.identity.federation
    * .core.interfaces.ProtocolContext)
    */
   public void renewToken(ProtocolContext context) throws ProcessingException
   {
      if (!(context instanceof WSTrustRequestContext))
         return;

      WSTrustRequestContext wstContext = (WSTrustRequestContext) context;
      // get the specified assertion that must be renewed.
      Element token = wstContext.getRequestSecurityToken().getRenewTargetElement();
      if (token == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "Invalid renew request: missing required RenewTarget");
      Element oldAssertionElement = (Element) token.getFirstChild();
      if (!this.isSAMLAssertion(oldAssertionElement))
         throw new ProcessingException(ErrorCodes.INVALID_ASSERTION
               + "RenewTarget doesn't not contain a SAMLV1.1 assertion");

      // get the JAXB representation of the old assertion.
      SAML11AssertionType oldAssertion = null;
      try
      {
         oldAssertion = SAMLUtil.saml11FromElement(oldAssertionElement);
      }
      catch (Exception je)
      {
         throw new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Error unmarshalling assertion", je);
      }

      // canceled assertions cannot be renewed.
      if (this.revocationRegistry.isRevoked(SAMLUtil.SAML11_TOKEN_TYPE, oldAssertion.getID()))
         throw new ProcessingException(ErrorCodes.ASSERTION_RENEWAL_EXCEPTION + "SAMLV1.1 Assertion with id "
               + oldAssertion.getID() + " has been canceled and cannot be renewed");

      // adjust the lifetime for the renewed assertion.
      SAML11ConditionsType conditions = oldAssertion.getConditions();
      conditions.setNotBefore(wstContext.getRequestSecurityToken().getLifetime().getCreated());
      conditions.setNotOnOrAfter(wstContext.getRequestSecurityToken().getLifetime().getExpires());

      // create a new unique ID for the renewed assertion.
      String assertionID = IDGenerator.create("ID_");

      // get the list of all assertion statements - should include the auth statement that contains the subject.
      List<SAML11StatementAbstractType> statements = new ArrayList<SAML11StatementAbstractType>();
      statements.addAll(oldAssertion.getStatements());

      // create the new assertion.
      SAML11AssertionType newAssertion = new SAML11AssertionType(assertionID, conditions.getNotBefore());
      newAssertion.addAllStatements(statements);
      newAssertion.setConditions(conditions);
      newAssertion.setIssuer(wstContext.getTokenIssuer());

      // create a security token with the new assertion.
      Element assertionElement = null;
      try
      {
         assertionElement = SAMLUtil.toElement(newAssertion);
      }
      catch (Exception e)
      {
         throw new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Failed to marshall SAMLV1.1 assertion", e);
      }
      SecurityToken securityToken = new StandardSecurityToken(wstContext.getRequestSecurityToken().getTokenType()
            .toString(), assertionElement, assertionID);
      wstContext.setSecurityToken(securityToken);

      // set the SAML assertion attached reference.
      KeyIdentifierType keyIdentifier = WSTrustUtil.createKeyIdentifier(SAMLUtil.SAML11_VALUE_TYPE, "#" + assertionID);
      Map<QName, String> attributes = new HashMap<QName, String>();
      attributes.put(new QName(WSTrustConstants.WSSE11_NS, "TokenType"), SAMLUtil.SAML11_TOKEN_TYPE);
      RequestedReferenceType attachedReference = WSTrustUtil.createRequestedReference(keyIdentifier, attributes);
      wstContext.setAttachedReference(attachedReference);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#validateToken(org.picketlink.identity
    * .federation.core.interfaces.ProtocolContext)
    */
   public void validateToken(ProtocolContext context) throws ProcessingException
   {
      if (!(context instanceof WSTrustRequestContext))
         return;

      WSTrustRequestContext wstContext = (WSTrustRequestContext) context;
      if (logger.isTraceEnabled())
         logger.trace("SAML V1.1 token validation started");

      // get the SAML assertion that must be validated.
      Element token = wstContext.getRequestSecurityToken().getValidateTargetElement();
      if (token == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "Bad validate request: missing required ValidateTarget");

      String code = WSTrustConstants.STATUS_CODE_VALID;
      String reason = "SAMLV1.1 Assertion successfuly validated";

      SAML11AssertionType assertion = null;
      Element assertionElement = (Element) token.getFirstChild();
      if (!this.isSAMLAssertion(assertionElement))
      {
         code = WSTrustConstants.STATUS_CODE_INVALID;
         reason = "Validation failure: supplied token is not a SAMLV1.1 Assertion";
      }
      else
      {
         try
         {
            assertion = SAMLUtil.saml11FromElement(assertionElement);
         }
         catch (Exception e)
         {
            throw new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Unmarshalling error:", e);
         }
      }

      // check if the assertion has been canceled before.
      if (this.revocationRegistry.isRevoked(SAMLUtil.SAML11_TOKEN_TYPE, assertion.getID()))
      {
         code = WSTrustConstants.STATUS_CODE_INVALID;
         reason = "Validation failure: assertion with id " + assertion.getID() + " has been canceled";
      }

      // check the assertion lifetime.
      try
      {
         if (AssertionUtil.hasExpired(assertion))
         {
            code = WSTrustConstants.STATUS_CODE_INVALID;
            reason = "Validation failure: assertion expired or used before its lifetime period";
         }
      }
      catch (Exception ce)
      {
         code = WSTrustConstants.STATUS_CODE_INVALID;
         reason = "Validation failure: unable to verify assertion lifetime: " + ce.getMessage();
      }

      // construct the status and set it on the request context.
      StatusType status = new StatusType();
      status.setCode(code);
      status.setReason(reason);
      wstContext.setStatus(status);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#family()
    */
   public String family()
   {
      return SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#getSupportedQName()
    */
   public QName getSupportedQName()
   {
      return new QName(tokenType(), JBossSAMLConstants.ASSERTION.get());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#supports(java.lang.String)
    */
   public boolean supports(String namespace)
   {
      return WSTrustConstants.BASE_NAMESPACE.equals(namespace);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#tokenType()
    */
   public String tokenType()
   {
      return SAMLUtil.SAML11_TOKEN_TYPE;
   }

   /**
    * <p>
    * Checks whether the specified element is a SAMLV1.1 assertion or not.
    * </p>
    * 
    * @param element
    *           the {@code Element} being verified.
    * @return {@code true} if the element is a SAMLV1.1 assertion; {@code false} otherwise.
    */
   private boolean isSAMLAssertion(Element element)
   {
      return element == null ? false : "Assertion".equals(element.getLocalName())
            && SAML11Constants.ASSERTION_11_NSURI.equals(element.getNamespaceURI());
   }

}
