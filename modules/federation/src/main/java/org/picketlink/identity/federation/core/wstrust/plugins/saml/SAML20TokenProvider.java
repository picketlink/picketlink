/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.sts.AbstractSecurityTokenProvider;
import org.picketlink.identity.federation.core.wstrust.SecurityToken;
import org.picketlink.identity.federation.core.wstrust.StandardSecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.KeyInfoConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A {@code SecurityTokenProvider} implementation that handles WS-Trust SAML 2.0 token requests.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SAML20TokenProvider extends AbstractSecurityTokenProvider implements SecurityTokenProvider {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private SAML20TokenAttributeProvider attributeProvider;

    private boolean useAbsoluteKeyIdentifier = false;

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#initialize(java.util.Map)
     */
    public void initialize(Map<String, String> properties) {
        super.initialize(properties);

        // Check if an attribute provider has been set.
        String attributeProviderClassName = this.properties.get(ATTRIBUTE_PROVIDER);
        if (attributeProviderClassName == null) {
            logger.trace("No attribute provider set");
        } else {
            try {
                Class<?> clazz = SecurityActions.loadClass(getClass(), attributeProviderClassName);
                Object object = clazz.newInstance();
                if (object instanceof SAML20TokenAttributeProvider) {
                    this.attributeProvider = (SAML20TokenAttributeProvider) object;
                    this.attributeProvider.setProperties(this.properties);
                } else
                    logger.stsWrongAttributeProviderTypeNotInstalled(attributeProviderClassName);
            } catch (Exception pae) {
                logger.attributeProviderInstationError(pae);
            }
        }

        String absoluteKI = this.properties.get(USE_ABSOLUTE_KEYIDENTIFIER);
        if (absoluteKI != null && "true".equalsIgnoreCase(absoluteKI)) {
            useAbsoluteKeyIdentifier = true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#
     * cancelToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
     */
    public void cancelToken(ProtocolContext protoContext) throws ProcessingException {
        if (!(protoContext instanceof WSTrustRequestContext))
            return;

        WSTrustRequestContext context = (WSTrustRequestContext) protoContext;

        // get the assertion that must be canceled.
        Element token = context.getRequestSecurityToken().getCancelTargetElement();
        if (token == null)
            throw logger.wsTrustNullCancelTargetError();
        Element assertionElement = (Element) token.getFirstChild();
        if (!this.isAssertion(assertionElement))
            throw logger.assertionInvalidError();

        // get the assertion ID and add it to the canceled assertions set.
        String assertionId = assertionElement.getAttribute("ID");
        this.revocationRegistry.revokeToken(SAMLUtil.SAML2_TOKEN_TYPE, assertionId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#
     * issueToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
     */
    public void issueToken(ProtocolContext protoContext) throws ProcessingException {
        if (!(protoContext instanceof WSTrustRequestContext))
            return;

        WSTrustRequestContext context = (WSTrustRequestContext) protoContext;
        // generate an id for the new assertion.
        String assertionID = IDGenerator.create("ID_");

        // lifetime and audience restrictions.
        Lifetime lifetime = context.getRequestSecurityToken().getLifetime();
        AudienceRestrictionType restriction = null;
        AppliesTo appliesTo = context.getRequestSecurityToken().getAppliesTo();
        if (appliesTo != null)
            restriction = SAMLAssertionFactory.createAudienceRestriction(WSTrustUtil.parseAppliesTo(appliesTo));
        ConditionsType conditions = SAMLAssertionFactory.createConditions(lifetime.getCreated(), lifetime.getExpires(),
                restriction);

        // the assertion principal (default is caller principal)
        Principal principal = context.getCallerPrincipal();

        String confirmationMethod = null;
        KeyInfoConfirmationDataType keyInfoDataType = null;
        // if there is a on-behalf-of principal, we have the sender vouches confirmation method.
        if (context.getOnBehalfOfPrincipal() != null) {
            principal = context.getOnBehalfOfPrincipal();
            confirmationMethod = SAMLUtil.SAML2_SENDER_VOUCHES_URI;
        }
        // if there is a proof-of-possession token in the context, we have the holder of key confirmation method.
        else if (context.getProofTokenInfo() != null) {
            confirmationMethod = SAMLUtil.SAML2_HOLDER_OF_KEY_URI;
            keyInfoDataType = SAMLAssertionFactory.createKeyInfoConfirmation(context.getProofTokenInfo());
        } else
            confirmationMethod = SAMLUtil.SAML2_BEARER_URI;

        SubjectConfirmationType subjectConfirmation = SAMLAssertionFactory.createSubjectConfirmation(null, confirmationMethod,
                keyInfoDataType);

        // create a subject using the caller principal or on-behalf-of principal.
        String subjectName = principal == null ? "ANONYMOUS" : principal.getName();
        NameIDType nameID = SAMLAssertionFactory.createNameID(null, "urn:picketlink:identity-federation", subjectName);
        SubjectType subject = SAMLAssertionFactory.createSubject(nameID, subjectConfirmation);


        List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();

        // create the attribute statements if necessary.
        Map<String, Object> claimedAttributes = context.getClaimedAttributes();
        if (claimedAttributes != null) {
            statements.add(StatementUtil.createAttributeStatement(claimedAttributes));
        }

        // create an AuthnStatement
        statements.add(StatementUtil.createAuthnStatement(lifetime.getCreated(), confirmationMethod));

        // create the SAML assertion.
        NameIDType issuerID = SAMLAssertionFactory.createNameID(null, null, context.getTokenIssuer());
        AssertionType assertion = SAMLAssertionFactory.createAssertion(assertionID, issuerID, lifetime.getCreated(),
                conditions, subject, statements);

        if (this.attributeProvider != null) {
            AttributeStatementType attributeStatement = this.attributeProvider.getAttributeStatement();
            if (attributeStatement != null) {
                assertion.addStatement(attributeStatement);
            }
        }

        // convert the constructed assertion to element.
        Element assertionElement = null;
        try {
            assertionElement = SAMLUtil.toElement(assertion);
        } catch (Exception e) {
            throw logger.samlAssertionMarshallError(e);
        }

        SecurityToken token = new StandardSecurityToken(context.getRequestSecurityToken().getTokenType().toString(),
                assertionElement, assertionID);
        context.setSecurityToken(token);

        // set the SAML assertion attached reference.
        String keyIdentifierValue = assertionID;
        if (!useAbsoluteKeyIdentifier) {
            keyIdentifierValue = "#" + keyIdentifierValue;
        }
        KeyIdentifierType keyIdentifier = WSTrustUtil.createKeyIdentifier(SAMLUtil.SAML2_VALUE_TYPE, keyIdentifierValue);
        Map<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(new QName(WSTrustConstants.WSSE11_NS, "TokenType", WSTrustConstants.WSSE.PREFIX_11),
                SAMLUtil.SAML2_TOKEN_TYPE);
        RequestedReferenceType attachedReference = WSTrustUtil.createRequestedReference(keyIdentifier, attributes);
        context.setAttachedReference(attachedReference);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#
     * renewToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
     */
    public void renewToken(ProtocolContext protoContext) throws ProcessingException {
        if (!(protoContext instanceof WSTrustRequestContext))
            return;

        WSTrustRequestContext context = (WSTrustRequestContext) protoContext;
        // get the specified assertion that must be renewed.
        Element token = context.getRequestSecurityToken().getRenewTargetElement();
        if (token == null)
            throw logger.wsTrustNullRenewTargetError();
        Element oldAssertionElement = (Element) token.getFirstChild();
        if (!this.isAssertion(oldAssertionElement))
            throw logger.assertionInvalidError();

        // get the JAXB representation of the old assertion.
        AssertionType oldAssertion = null;
        try {
            oldAssertion = SAMLUtil.fromElement(oldAssertionElement);
        } catch (Exception je) {
            throw logger.samlAssertionUnmarshallError(je);
        }

        // canceled assertions cannot be renewed.
        if (this.revocationRegistry.isRevoked(SAMLUtil.SAML2_TOKEN_TYPE, oldAssertion.getID()))
            throw logger.samlAssertionRevokedCouldNotRenew(oldAssertion.getID());

        // adjust the lifetime for the renewed assertion.
        ConditionsType conditions = oldAssertion.getConditions();
        conditions.setNotBefore(context.getRequestSecurityToken().getLifetime().getCreated());
        conditions.setNotOnOrAfter(context.getRequestSecurityToken().getLifetime().getExpires());

        // create a new unique ID for the renewed assertion.
        String assertionID = IDGenerator.create("ID_");

        List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
        statements.addAll(oldAssertion.getStatements());

        // create the new assertion.
        AssertionType newAssertion = SAMLAssertionFactory.createAssertion(assertionID, oldAssertion.getIssuer(), context
                .getRequestSecurityToken().getLifetime().getCreated(), conditions, oldAssertion.getSubject(), statements);

        // create a security token with the new assertion.
        Element assertionElement = null;
        try {
            assertionElement = SAMLUtil.toElement(newAssertion);
        } catch (Exception e) {
            throw logger.samlAssertionMarshallError(e);
        }
        SecurityToken securityToken = new StandardSecurityToken(context.getRequestSecurityToken().getTokenType().toString(),
                assertionElement, assertionID);
        context.setSecurityToken(securityToken);

        // set the SAML assertion attached reference.
        KeyIdentifierType keyIdentifier = WSTrustUtil.createKeyIdentifier(SAMLUtil.SAML2_VALUE_TYPE, "#" + assertionID);
        Map<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(new QName(WSTrustConstants.WSSE11_NS, "TokenType"), SAMLUtil.SAML2_TOKEN_TYPE);
        RequestedReferenceType attachedReference = WSTrustUtil.createRequestedReference(keyIdentifier, attributes);
        context.setAttachedReference(attachedReference);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#
     * validateToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
     */
    public void validateToken(ProtocolContext protoContext) throws ProcessingException {
        if (!(protoContext instanceof WSTrustRequestContext))
            return;

        WSTrustRequestContext context = (WSTrustRequestContext) protoContext;

        logger.trace("SAML token validation started");

        // get the SAML assertion that must be validated.
        Element token = context.getRequestSecurityToken().getValidateTargetElement();
        if (token == null)
            throw logger.wsTrustNullValidationTargetError();

        String code = WSTrustConstants.STATUS_CODE_VALID;
        String reason = "SAMLV2.0 Assertion successfuly validated";

        AssertionType assertion = null;
        Element assertionElement = (Element) token.getFirstChild();
        if (!this.isAssertion(assertionElement)) {
            code = WSTrustConstants.STATUS_CODE_INVALID;
            reason = "Validation failure: supplied token is not a SAMLV2.0 Assertion";
        } else {
            try {
                if (logger.isTraceEnabled()) {
                    logger.samlAssertion(DocumentUtil.getNodeAsString(assertionElement));
                }
                assertion = SAMLUtil.fromElement(assertionElement);
            } catch (Exception e) {
                throw logger.samlAssertionUnmarshallError(e);
            }
        }

        // check if the assertion has been canceled before.
        if (this.revocationRegistry.isRevoked(SAMLUtil.SAML2_TOKEN_TYPE, assertion.getID())) {
            code = WSTrustConstants.STATUS_CODE_INVALID;
            reason = "Validation failure: assertion with id " + assertion.getID() + " has been canceled";
        }

        // check the assertion lifetime.
        try {
            if (AssertionUtil.hasExpired(assertion)) {
                code = WSTrustConstants.STATUS_CODE_INVALID;
                reason = "Validation failure: assertion expired or used before its lifetime period";
            }
        } catch (Exception ce) {
            code = WSTrustConstants.STATUS_CODE_INVALID;
            reason = "Validation failure: unable to verify assertion lifetime: " + ce.getMessage();
        }

        // construct the status and set it on the request context.
        StatusType status = new StatusType();
        status.setCode(code);
        status.setReason(reason);
        context.setStatus(status);
    }

    /**
     * <p>
     * Checks whether the specified element is a SAMLV2.0 assertion or not.
     * </p>
     *
     * @param element the {@code Element} being verified.
     *
     * @return {@code true} if the element is a SAMLV2.0 assertion; {@code false} otherwise.
     */
    private boolean isAssertion(Element element) {
        return element == null ? false : "Assertion".equals(element.getLocalName())
                && WSTrustConstants.SAML2_ASSERTION_NS.equals(element.getNamespaceURI());
    }

    /**
     * @see {@code SecurityTokenProvider#supports(String)}
     */
    public boolean supports(String namespace) {
        return WSTrustConstants.BASE_NAMESPACE.equals(namespace);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#tokenType()
     */
    public String tokenType() {
        return SAMLUtil.SAML2_TOKEN_TYPE;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#getSupportedQName()
     */
    public QName getSupportedQName() {
        return new QName(tokenType(), JBossSAMLConstants.ASSERTION.get());
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#family()
     */
    public String family() {
        return SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();
    }
}