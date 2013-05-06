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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.constants.AttributeConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.X500SAMLProfileConstants;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextClassRefType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType.AuthnContextTypeSequence;

/**
 * Deals with SAML2 Statements
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public class StatementUtil {
    public static final QName X500_QNAME = new QName(JBossSAMLURIConstants.X500_NSURI.get(), "Encoding",
            JBossSAMLURIConstants.X500_PREFIX.get());

    /**
     * Create an AuthnStatementType given the issue instant and the type of authentication
     *
     * @param instant an instanceof {@link XMLGregorianCalendar}
     * @param authnContextClassRefValue indicate the type of authentication performed
     * @return {@link AuthnStatementType}
     */
    public static AuthnStatementType createAuthnStatement(XMLGregorianCalendar instant, String authnContextClassRefValue) {
        AuthnStatementType authnStatement = new AuthnStatementType(instant);

        AuthnContextType authnContext = new AuthnContextType();
        AuthnContextClassRefType authnContextClassRef = new AuthnContextClassRefType(URI.create(authnContextClassRefValue));

        AuthnContextTypeSequence sequence = (authnContext).new AuthnContextTypeSequence();
        sequence.setClassRef(authnContextClassRef);
        authnContext.setSequence(sequence);

        authnStatement.setAuthnContext(authnContext);

        return authnStatement;
    }

    /**
     * Create an attribute statement with all the attributes
     *
     * @param attributes a map with keys from {@link AttributeConstants}
     * @return
     */
    public static AttributeStatementType createAttributeStatement(Map<String, Object> attributes) {
        AttributeStatementType attrStatement = null;

        int i = 0;

        Set<String> keys = attributes.keySet();
        for (String key : keys) {
            if (i == 0) {
                // Deal with the X500 Profile of SAML2
                attrStatement = new AttributeStatementType();
                i++;
            }

            // if the attribute contains roles, add each role as an attribute.
            if (AttributeConstants.ROLES.equalsIgnoreCase(key)) {
                Object value = attributes.get(key);
                if (value instanceof Collection<?>) {
                    Collection<?> roles = (Collection<?>) value;
                    for (Object role : roles) {
                        AttributeType roleAttr = new AttributeType("Role");
                        roleAttr.addAttributeValue(role);
                        attrStatement.addAttribute(new ASTChoiceType(roleAttr));
                    }
                }
            }

            else {
                AttributeType att;
                Object value = attributes.get(key);

                String uri = X500SAMLProfileConstants.getOID(key);
                if (StringUtil.isNotNull(uri)) {
                    att = getX500Attribute(uri);
                    att.setFriendlyName(key);
                } else {
                    att = new AttributeType(key);
                    att.setFriendlyName(key);
                    att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get());
                }

                att.addAttributeValue(value);
                attrStatement.addAttribute(new ASTChoiceType(att));
            }
        }
        return attrStatement;
    }

    /**
     * Given a set of roles, create an attribute statement
     *
     * @param roles
     * @return
     */
    public static AttributeStatementType createAttributeStatement(List<String> roles) {
        AttributeStatementType attrStatement = null;
        for (String role : roles) {
            if(attrStatement == null){
                attrStatement = new AttributeStatementType();
            }
            AttributeType attr = new AttributeType("Role");
            attr.addAttributeValue(role);
            attrStatement.addAttribute(new ASTChoiceType(attr));
        }
        return attrStatement;
    }

    /**
     * Given an attribute type and a value, create {@link AttributeStatementType}
     *
     * @param key attribute type
     * @param value attribute value
     * @return
     */
    public static AttributeStatementType createAttributeStatement(String key, String value) {
        AttributeStatementType attrStatement = new AttributeStatementType();
        AttributeType attr = new AttributeType(key);
        attr.addAttributeValue(value);
        attrStatement.addAttribute(new ASTChoiceType(attr));

        return attrStatement;
    }

    private static AttributeType getX500Attribute(String name) {
        AttributeType att = new AttributeType(name);
        att.getOtherAttributes().put(X500_QNAME, "LDAP");

        att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get());
        return att;
    }
}