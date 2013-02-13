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
package org.picketlink.identity.federation.core.saml.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionsType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;

/**
 * <p>
 * A SAML2 specification based instance of {@code ProtocolContext}
 * </p>
 * <p>
 * This instance is used to pass information from the IDP to the Core STS.
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 30, 2010
 */
public class SAML11ProtocolContext implements ProtocolContext {
    protected String issuerID;

    protected SAML11SubjectType subjectType;

    protected SAML11ConditionsType conditions;

    protected List<SAML11StatementAbstractType> statements = new ArrayList<SAML11StatementAbstractType>();

    protected SAML11AssertionType issuedAssertion;

    protected String authMethod = SAML11Constants.AUTH_METHOD_PASSWORD;

    /**
     * Get the Issuer ID
     *
     * @return instance of {@link SAML11NameIdentifierType}
     */
    public String getIssuerID() {
        return issuerID;
    }

    /**
     * Set the Issuer ID
     *
     * @param issuerID {@link String}
     */
    public void setIssuerID(String issuerID) {
        this.issuerID = issuerID;
    }

    /**
     * Get the subject
     *
     * @return {@link SAML11SubjectType}
     */
    public SAML11SubjectType getSubjectType() {
        return subjectType;
    }

    /**
     * Set the subject
     *
     * @param subjectType {@link SAML11SubjectType}
     */
    public void setSubjectType(SAML11SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    /**
     * Get the conditions
     *
     * @return {@link SAML11ConditionsType}
     */
    public SAML11ConditionsType getConditions() {
        return conditions;
    }

    /**
     * Set the conditions
     *
     * @param conditions {@link SAML11ConditionsType}
     */
    public void setConditions(SAML11ConditionsType conditions) {
        this.conditions = conditions;
    }

    /**
     * Get the statements as a read-only list
     *
     * @return {@link SAML11StatementAbstractType}
     */
    public List<SAML11StatementAbstractType> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    /**
     * Add a list of statements
     *
     * @param statements {@link SAML11StatementAbstractType}
     */
    public void setStatements(List<SAML11StatementAbstractType> statements) {
        this.statements = statements;
    }

    /**
     * Get the previously issued assertion by the STS
     *
     * @return {@link SAML11AssertionType}
     */
    public SAML11AssertionType getIssuedAssertion() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        return issuedAssertion;
    }

    /**
     * Set the assertion issued by the STS
     *
     * @param issuedAssertion {@link SAML11AssertionType}
     */
    public void setIssuedAssertion(SAML11AssertionType issuedAssertion) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        this.issuedAssertion = issuedAssertion;
    }

    /**
     * Get the Authentication Method
     *
     * @return
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Set the Authentication Method. By default, it is set to urn:oasis:names:tc:SAML:1.0:am:password
     *
     * @param authMethod
     */
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#serviceName()
     */
    public String serviceName() {
        return null;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#tokenType()
     */
    public String tokenType() {
        return SAML11Constants.ASSERTION_11_NSURI;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#getQName()
     */
    public QName getQName() {
        String localPart = JBossSAMLConstants.ASSERTION.get();
        String ns = tokenType();
        return new QName(ns, localPart);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#family()
     */
    public String family() {
        return SecurityTokenProvider.FAMILY_TYPE.SAML11.toString();
    }
}