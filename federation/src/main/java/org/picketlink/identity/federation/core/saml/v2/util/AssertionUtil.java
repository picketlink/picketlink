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
package org.picketlink.identity.federation.core.saml.v2.util;

import java.io.ByteArrayOutputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.IssueInstantMissingException;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionsType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility to deal with assertions
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 3, 2009
 */
public class AssertionUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Given {@code AssertionType}, convert it into a String
     *
     * @param assertion
     * @return
     * @throws ProcessingException
     */
    public static String asString(AssertionType assertion) throws ProcessingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(assertion);
        return new String(baos.toByteArray());
    }

    /**
     * Create an assertion
     *
     * @param id
     * @param issuer
     * @return
     */
    public static SAML11AssertionType createSAML11Assertion(String id, XMLGregorianCalendar issueInstant, String issuer) {
        SAML11AssertionType assertion = new SAML11AssertionType(id, issueInstant);
        assertion.setIssuer(issuer);
        return assertion;
    }

    /**
     * Create an assertion
     *
     * @param id
     * @param issuer
     * @return
     */
    public static AssertionType createAssertion(String id, NameIDType issuer) {
        XMLGregorianCalendar issueInstant = null;
        try {
            issueInstant = XMLTimeUtil.getIssueInstant();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        AssertionType assertion = new AssertionType(id, issueInstant);
        assertion.setIssuer(issuer);
        return assertion;
    }

    /**
     * Given a user name, create a {@code SubjectType} that can then be inserted into an assertion
     *
     * @param userName
     * @return
     */
    public static SubjectType createAssertionSubject(String userName) {
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue(userName);
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        return assertionSubject;
    }

    /**
     * Create an attribute type
     *
     * @param name Name of the attribute
     * @param nameFormat name format uri
     * @param attributeValues an object array of attribute values
     * @return
     */
    public static AttributeType createAttribute(String name, String nameFormat, Object... attributeValues) {
        AttributeType att = new AttributeType(name);
        att.setNameFormat(nameFormat);
        if (attributeValues != null && attributeValues.length > 0) {
            for (Object attributeValue : attributeValues) {
                att.addAttributeValue(attributeValue);
            }
        }

        return att;
    }

    /**
     * <p>
     * Add validity conditions to the SAML2 Assertion
     * </p>
     * <p>
     * There is no clock skew added.
     *
     * @see {{@link #createTimedConditions(AssertionType, long, long)}
     *      </p>
     * @param assertion
     * @param durationInMilis
     * @throws ConfigurationException
     * @throws IssueInstantMissingException
     */
    public static void createTimedConditions(AssertionType assertion, long durationInMilis) throws ConfigurationException,
            IssueInstantMissingException {
        XMLGregorianCalendar issueInstant = assertion.getIssueInstant();
        if (issueInstant == null)
            throw new IssueInstantMissingException(ErrorCodes.NULL_ISSUE_INSTANT);
        XMLGregorianCalendar assertionValidityLength = XMLTimeUtil.add(issueInstant, durationInMilis);
        ConditionsType conditionsType = new ConditionsType();
        conditionsType.setNotBefore(issueInstant);
        conditionsType.setNotOnOrAfter(assertionValidityLength);

        assertion.setConditions(conditionsType);
    }

    /**
     * Add validity conditions to the SAML2 Assertion
     *
     * @param assertion
     * @param durationInMilis
     * @throws ConfigurationException
     * @throws IssueInstantMissingException
     */
    public static void createTimedConditions(AssertionType assertion, long durationInMilis, long clockSkew)
            throws ConfigurationException, IssueInstantMissingException {
        XMLGregorianCalendar issueInstant = assertion.getIssueInstant();
        if (issueInstant == null)
            throw logger.samlIssueInstantMissingError();
        XMLGregorianCalendar assertionValidityLength = XMLTimeUtil.add(issueInstant, durationInMilis + clockSkew);

        ConditionsType conditionsType = new ConditionsType();

        XMLGregorianCalendar beforeInstant = XMLTimeUtil.subtract(issueInstant, clockSkew);

        conditionsType.setNotBefore(beforeInstant);
        conditionsType.setNotOnOrAfter(assertionValidityLength);

        assertion.setConditions(conditionsType);
    }

    /**
     * Add validity conditions to the SAML2 Assertion
     *
     * @param assertion
     * @param durationInMilis
     * @throws ConfigurationException
     * @throws IssueInstantMissingException
     */
    public static void createSAML11TimedConditions(SAML11AssertionType assertion, long durationInMilis, long clockSkew)
            throws ConfigurationException, IssueInstantMissingException {
        XMLGregorianCalendar issueInstant = assertion.getIssueInstant();
        if (issueInstant == null)
            throw new IssueInstantMissingException(ErrorCodes.NULL_ISSUE_INSTANT);
        XMLGregorianCalendar assertionValidityLength = XMLTimeUtil.add(issueInstant, durationInMilis + clockSkew);

        SAML11ConditionsType conditionsType = new SAML11ConditionsType();

        XMLGregorianCalendar beforeInstant = XMLTimeUtil.subtract(issueInstant, clockSkew);

        conditionsType.setNotBefore(beforeInstant);
        conditionsType.setNotOnOrAfter(assertionValidityLength);
        assertion.setConditions(conditionsType);
    }

    /**
     * Given an assertion element, validate the signature
     *
     * @param assertionElement
     * @param publicKey the {@link PublicKey}
     * @return
     */
    public static boolean isSignatureValid(Element assertionElement, PublicKey publicKey) {
        try {
            Document doc = DocumentUtil.createDocument();
            Node n = doc.importNode(assertionElement, true);
            doc.appendChild(n);

            return new SAML2Signature().validate(doc, publicKey);
        } catch (Exception e) {
            logger.signatureAssertionValidationError(e);
        }
        return false;
    }

    /**
     * Check whether the assertion has expired
     *
     * @param assertion
     * @return
     * @throws ConfigurationException
     */
    public static boolean hasExpired(AssertionType assertion) throws ConfigurationException {
        boolean expiry = false;

        // Check for validity of assertion
        ConditionsType conditionsType = assertion.getConditions();
        if (conditionsType != null) {
            XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
            XMLGregorianCalendar notBefore = conditionsType.getNotBefore();
            XMLGregorianCalendar notOnOrAfter = conditionsType.getNotOnOrAfter();

            logger.trace("Now=" + now.toXMLFormat() + " ::notBefore=" + notBefore.toXMLFormat() + " ::notOnOrAfter=" + notOnOrAfter);
            
            expiry = !XMLTimeUtil.isValid(now, notBefore, notOnOrAfter);
            
            if (expiry) {
                logger.samlAssertionExpired(assertion.getID());
            }
        }

        // TODO: if conditions do not exist, assume the assertion to be everlasting?
        return expiry;
    }

    /**
     * Verify whether the assertion has expired. You can add in a clock skew to adapt to conditions where in the IDP and SP are
     * out of sync.
     *
     * @param assertion
     * @param clockSkewInMilis in miliseconds
     * @return
     * @throws ConfigurationException
     */
    public static boolean hasExpired(AssertionType assertion, long clockSkewInMilis) throws ConfigurationException {
        boolean expiry = false;

        // Check for validity of assertion
        ConditionsType conditionsType = assertion.getConditions();
        if (conditionsType != null) {
            XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
            XMLGregorianCalendar notBefore = conditionsType.getNotBefore();
            XMLGregorianCalendar updatedNotBefore = XMLTimeUtil.subtract(notBefore, clockSkewInMilis);
            XMLGregorianCalendar notOnOrAfter = conditionsType.getNotOnOrAfter();
            XMLGregorianCalendar updatedOnOrAfter = XMLTimeUtil.add(notOnOrAfter, clockSkewInMilis);

            logger.trace("Now=" + now.toXMLFormat() + " ::notBefore=" + notBefore.toXMLFormat() + " ::notOnOrAfter=" + notOnOrAfter);
            expiry = !XMLTimeUtil.isValid(now, updatedNotBefore, updatedOnOrAfter);
            if (expiry) {
                logger.samlAssertionExpired(assertion.getID());
            }
        }

        // TODO: if conditions do not exist, assume the assertion to be everlasting?
        return expiry;
    }

    /**
     * Check whether the assertion has expired
     *
     * @param assertion
     * @return
     * @throws ConfigurationException
     */
    public static boolean hasExpired(SAML11AssertionType assertion) throws ConfigurationException {
        boolean expiry = false;

        // Check for validity of assertion
        SAML11ConditionsType conditionsType = assertion.getConditions();
        if (conditionsType != null) {
            XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
            XMLGregorianCalendar notBefore = conditionsType.getNotBefore();
            XMLGregorianCalendar notOnOrAfter = conditionsType.getNotOnOrAfter();
            
            logger.trace("Now=" + now.toXMLFormat() + " ::notBefore=" + notBefore.toXMLFormat() + " ::notOnOrAfter=" + notOnOrAfter);
            
            expiry = !XMLTimeUtil.isValid(now, notBefore, notOnOrAfter);
            if (expiry) {
                logger.samlAssertionExpired(assertion.getID());
            }
        }

        // TODO: if conditions do not exist, assume the assertion to be everlasting?
        return expiry;
    }

    /**
     * Verify whether the assertion has expired. You can add in a clock skew to adapt to conditions where in the IDP and SP are
     * out of sync.
     *
     * @param assertion
     * @param clockSkewInMilis in miliseconds
     * @return
     * @throws ConfigurationException
     */
    public static boolean hasExpired(SAML11AssertionType assertion, long clockSkewInMilis) throws ConfigurationException {
        boolean expiry = false;

        // Check for validity of assertion
        SAML11ConditionsType conditionsType = assertion.getConditions();
        if (conditionsType != null) {
            XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
            XMLGregorianCalendar notBefore = conditionsType.getNotBefore();
            XMLGregorianCalendar updatedNotBefore = XMLTimeUtil.subtract(notBefore, clockSkewInMilis);
            XMLGregorianCalendar notOnOrAfter = conditionsType.getNotOnOrAfter();
            XMLGregorianCalendar updatedOnOrAfter = XMLTimeUtil.add(notOnOrAfter, clockSkewInMilis);

            logger.trace("Now=" + now.toXMLFormat() + " ::notBefore=" + notBefore.toXMLFormat() + " ::notOnOrAfter=" + notOnOrAfter);
            
            expiry = !XMLTimeUtil.isValid(now, updatedNotBefore, updatedOnOrAfter);
            if (expiry) {
                logger.samlAssertionExpired(assertion.getID());
            }
        }

        // TODO: if conditions do not exist, assume the assertion to be everlasting?
        return expiry;
    }

    /**
     * Extract the expiration time from an {@link AssertionType}
     *
     * @param assertion
     * @return
     */
    public static XMLGregorianCalendar getExpiration(AssertionType assertion) {
        XMLGregorianCalendar expiry = null;

        ConditionsType conditionsType = assertion.getConditions();
        if (conditionsType != null) {
            expiry = conditionsType.getNotOnOrAfter();
        }
        return expiry;
    }

    /**
     * Given an assertion, return the list of roles it may have
     *
     * @param assertion The {@link AssertionType}
     * @param roleKeys a list of string values representing the role keys. The list can be null.
     * @return
     */
    public static List<String> getRoles(AssertionType assertion, List<String> roleKeys) {
        List<String> roles = new ArrayList<String>();
        Set<StatementAbstractType> statements = assertion.getStatements();
        for (StatementAbstractType statement : statements) {
            if (statement instanceof AttributeStatementType) {
                AttributeStatementType attributeStatement = (AttributeStatementType) statement;
                List<ASTChoiceType> attList = attributeStatement.getAttributes();
                for (ASTChoiceType obj : attList) {
                    AttributeType attr = obj.getAttribute();
                    if (roleKeys != null && roleKeys.size() > 0) {
                        if (!roleKeys.contains(attr.getName()))
                            continue;
                    }
                    List<Object> attributeValues = attr.getAttributeValue();
                    if (attributeValues != null) {
                        for (Object attrValue : attributeValues) {
                            if (attrValue instanceof String) {
                                roles.add((String) attrValue);
                            } else if (attrValue instanceof Node) {
                                Node roleNode = (Node) attrValue;
                                roles.add(roleNode.getFirstChild().getNodeValue());
                            } else
                                throw logger.unknownObjectType(attrValue);
                        }
                    }
                }
            }
        }
        return roles;
    }

    /**
     * Given an assertion, return the list of roles it may have
     *
     * @param assertion The {@link SAML11AssertionType}
     * @param roleKeys a list of string values representing the role keys. The list can be null.
     * @return
     */
    public static List<String> getRoles(SAML11AssertionType assertion, List<String> roleKeys) {
        List<String> roles = new ArrayList<String>();
        List<SAML11StatementAbstractType> statements = assertion.getStatements();
        for (SAML11StatementAbstractType statement : statements) {
            if (statement instanceof SAML11AttributeStatementType) {
                SAML11AttributeStatementType attributeStatement = (SAML11AttributeStatementType) statement;
                List<SAML11AttributeType> attributes = attributeStatement.get();
                for (SAML11AttributeType attr : attributes) {
                    if (roleKeys != null && roleKeys.size() > 0) {
                        if (!roleKeys.contains(attr.getAttributeName()))
                            continue;
                    }
                    List<Object> attributeValues = attr.get();
                    if (attributeValues != null) {
                        for (Object attrValue : attributeValues) {
                            if (attrValue instanceof String) {
                                roles.add((String) attrValue);
                            } else if (attrValue instanceof Node) {
                                Node roleNode = (Node) attrValue;
                                roles.add(roleNode.getFirstChild().getNodeValue());
                            } else
                                throw logger.unknownObjectType(attrValue);
                        }
                    }
                }
            }
        }
        return roles;
    }
}