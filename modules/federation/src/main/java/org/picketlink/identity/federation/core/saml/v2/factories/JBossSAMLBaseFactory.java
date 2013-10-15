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
package org.picketlink.identity.federation.core.saml.v2.factories;

import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Base methods for the factories
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class JBossSAMLBaseFactory {

    /**
     * Create an empty attribute statement
     *
     * @return
     */
    public static AttributeStatementType createAttributeStatement() {
        return new AttributeStatementType();
    }

    /**
     * Create an attribute type given a role name
     *
     * @param roleName
     *
     * @return
     */
    public static AttributeType createAttributeForRole(String roleName) {
        AttributeType att = new AttributeType("role");
        att.setFriendlyName("role");
        att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());

        // rolename
        att.addAttributeValue(roleName);

        return att;
    }

    /**
     * Create an AttributeStatement given an attribute
     *
     * @param attributeValue
     *
     * @return
     */
    public static AttributeStatementType createAttributeStatement(String attributeValue) {
        AttributeStatementType attribStatement = new AttributeStatementType();
        AttributeType att = new AttributeType(attributeValue);
        att.addAttributeValue(attributeValue);

        attribStatement.addAttribute(new ASTChoiceType(att));
        return attribStatement;
    }

    /**
     * Create a Subject confirmation type given the method
     *
     * @param method
     *
     * @return
     */
    public static SubjectConfirmationType createSubjectConfirmation(String method) {
        SubjectConfirmationType sct = new SubjectConfirmationType();
        sct.setMethod(method);
        return sct;
    }

    /**
     * Create a Subject Confirmation
     *
     * @param inResponseTo
     * @param destinationURI
     * @param issueInstant
     *
     * @return
     */
    public static SubjectConfirmationDataType createSubjectConfirmationData(String inResponseTo, String destinationURI,
                                                                            XMLGregorianCalendar issueInstant) {
        SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setRecipient(destinationURI);
        //subjectConfirmationData.setNotBefore(issueInstant);
        subjectConfirmationData.setNotOnOrAfter(issueInstant);

        return subjectConfirmationData;
    }

    /**
     * Get a UUID String
     *
     * @return
     */
    public static String createUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Return the NameIDType for the issuer
     *
     * @param issuerID
     *
     * @return
     */
    public static NameIDType getIssuer(String issuerID) {
        NameIDType nid = new NameIDType();
        nid.setValue(issuerID);
        return nid;
    }
}