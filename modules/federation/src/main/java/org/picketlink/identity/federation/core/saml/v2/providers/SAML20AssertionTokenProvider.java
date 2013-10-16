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
package org.picketlink.identity.federation.core.saml.v2.providers;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.IssueInstantMissingException;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLProtocolContext;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.sts.AbstractSecurityTokenProvider;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A {@code SecurityTokenProvider} implementation for the SAML2 Specification.
 * </p>
 * <p>
 * This token provider does not handle the SAML20 Token Profile of the Oasis WS-Trust Specification.
 *
 * @author Anil.Saldhana@redhat.com
 * @see {@code SAML20TokenProvider}
 *      </p>
 *      <p>
 *      Configurable Properties are:
 *      </p>
 *      <p>
 *      ASSERTION_VALIDITY: specify the validity of the assertion in miliseconds. (Example: 5000 = 5secs)
 *      </p>
 *      <p>
 *      CLOCK_SKEW: specify the clock skew of the conditions for assertion in miliseconds. (Example: 2000 = 2secs)
 *      </p>
 * @since Dec 30, 2010
 */
public class SAML20AssertionTokenProvider extends AbstractSecurityTokenProvider implements SecurityTokenProvider {

    public static final String NS = JBossSAMLURIConstants.ASSERTION_NSURI.get();

    private long ASSERTION_VALIDITY = 5000; // 5secs in milis

    private long CLOCK_SKEW = 2000; // 2secs

    public void initialize(Map<String, String> props) {
        super.initialize(props);

        String validity = this.properties.get(GeneralConstants.ASSERTIONS_VALIDITY);
        if (validity != null) {
            ASSERTION_VALIDITY = Long.parseLong(validity);
        }
        String skew = this.properties.get(GeneralConstants.CLOCK_SKEW);
        if (skew != null) {
            CLOCK_SKEW = Long.parseLong(skew);
        }
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#supports(java.lang.String)
     */
    public boolean supports(String namespace) {
        return NS.equals(namespace);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#issueToken(org.picketlink.identity.federation.core.interfaces.ProtocolContext)
     */
    public void issueToken(ProtocolContext context) throws ProcessingException {
        if (!(context instanceof SAMLProtocolContext))
            return;

        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        SAMLProtocolContext samlProtocolContext = (SAMLProtocolContext) context;

        NameIDType issuerID = samlProtocolContext.getIssuerID();
        XMLGregorianCalendar issueInstant;
        try {
            issueInstant = XMLTimeUtil.getIssueInstant();
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        }
        ConditionsType conditions = samlProtocolContext.getConditions();
        SubjectType subject = samlProtocolContext.getSubjectType();
        List<StatementAbstractType> statements = samlProtocolContext.getStatements();

        // generate an id for the new assertion.
        String assertionID = IDGenerator.create("ID_");

        AssertionType assertionType = SAMLAssertionFactory.createAssertion(assertionID, issuerID, issueInstant, conditions,
                subject, statements);

        try {
            AssertionUtil.createTimedConditions(assertionType, ASSERTION_VALIDITY, CLOCK_SKEW);
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        } catch (IssueInstantMissingException e) {
            throw logger.processingError(e);
        }

        try {
            this.tokenRegistry.addToken(assertionID, assertionType);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
        samlProtocolContext.setIssuedAssertion(assertionType);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#renewToken(org.picketlink.identity.federation.core.interfaces.ProtocolContext)
     */
    public void renewToken(ProtocolContext context) throws ProcessingException {
        if (!(context instanceof SAMLProtocolContext))
            return;

        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        SAMLProtocolContext samlProtocolContext = (SAMLProtocolContext) context;

        AssertionType issuedAssertion = samlProtocolContext.getIssuedAssertion();

        try {
            XMLGregorianCalendar currentTime = XMLTimeUtil.getIssueInstant();
            issuedAssertion.updateIssueInstant(currentTime);
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        }

        try {
            AssertionUtil.createTimedConditions(issuedAssertion, ASSERTION_VALIDITY, CLOCK_SKEW);
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        } catch (IssueInstantMissingException e) {
            throw logger.processingError(e);
        }

        try {
            this.tokenRegistry.addToken(issuedAssertion.getID(), issuedAssertion);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
        samlProtocolContext.setIssuedAssertion(issuedAssertion);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#cancelToken(org.picketlink.identity.federation.core.interfaces.ProtocolContext)
     */
    public void cancelToken(ProtocolContext context) throws ProcessingException {
        if (!(context instanceof SAMLProtocolContext))
            return;

        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        SAMLProtocolContext samlProtocolContext = (SAMLProtocolContext) context;
        AssertionType issuedAssertion = samlProtocolContext.getIssuedAssertion();
        try {
            this.tokenRegistry.removeToken(issuedAssertion.getID());
        } catch (IOException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#validateToken(org.picketlink.identity.federation.core.interfaces.ProtocolContext)
     */
    public void validateToken(ProtocolContext context) throws ProcessingException {
        if (!(context instanceof SAMLProtocolContext))
            return;

        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        SAMLProtocolContext samlProtocolContext = (SAMLProtocolContext) context;

        AssertionType issuedAssertion = samlProtocolContext.getIssuedAssertion();

        try {
            if (!AssertionUtil.hasExpired(issuedAssertion))
                throw logger.samlAssertionExpiredError();
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        }

        if (issuedAssertion == null)
            throw logger.assertionInvalidError();
        if (this.tokenRegistry.getToken(issuedAssertion.getID()) == null)
            throw logger.assertionInvalidError();
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#tokenType()
     */
    public String tokenType() {
        return NS;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#getSupportedQName()
     */
    public QName getSupportedQName() {
        return new QName(NS, JBossSAMLConstants.ASSERTION.get());
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#family()
     */
    public String family() {
        return SecurityTokenProvider.FAMILY_TYPE.SAML2.toString();
    }
}