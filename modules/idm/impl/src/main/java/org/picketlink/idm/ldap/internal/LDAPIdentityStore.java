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
package org.picketlink.idm.ldap.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.ldap.annotations.LDAPEntry;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;

/**
 * An IdentityStore implementation backed by an LDAP directory
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({LDAPPlainTextPasswordCredentialHandler.class})
public class LDAPIdentityStore extends AbstractIdentityStore<LDAPIdentityStoreConfiguration> {

    private LDAPOperationManager operationManager;

    @Override
    public void setup(LDAPIdentityStoreConfiguration config) {
        super.setup(config);

        try {
            this.operationManager = new LDAPOperationManager(getConfig());
        } catch (NamingException e) {
            throw IDMMessages.MESSAGES.ldapCouldNotCreateContext(e);
        }
    }

    @Override
    public void add(IdentityContext context, AttributedType value) {
        LDAPEntry ldapEntryConfig = value.getClass().getAnnotation(LDAPEntry.class);
        BasicAttributes ldapEntryAttributes = new BasicAttributes();

        BasicAttribute objectClassAttribute = new BasicAttribute(OBJECT_CLASS);

        for (String objectClassValue : ldapEntryConfig.objectClass()) {
            objectClassAttribute.add(objectClassValue);
        }

        ldapEntryAttributes.put(objectClassAttribute);

        List<Property<String>> properties = PropertyQueries
                .<String>createQuery(value.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

        String bindingName = null;

        for (Property<String> property : properties) {
            AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);
            String propertyValue = property.getValue(value);

            if (!isNullOrEmpty(attributeProperty.mappedName())) {
                if (propertyValue == null) {
                    propertyValue = " ";
                }

                ldapEntryAttributes.put(attributeProperty.mappedName(), propertyValue);

                if (attributeProperty.mappedName().equalsIgnoreCase(ldapEntryConfig.id())) {
                    bindingName = ldapEntryConfig.id() + LDAPConstants.EQUAL + propertyValue + LDAPConstants.COMMA + ldapEntryConfig.baseDN();
                }
            }
        }

        this.operationManager.createSubContext(bindingName, ldapEntryAttributes);
        NamingEnumeration<SearchResult> search = null;

        try {
            search = this.operationManager.search(getConfig().getBaseDN(), "(&(ldapEntryConfig=*)(" + bindingName + "))");

            SearchResult next = search.next();

            javax.naming.directory.Attribute attribute = next.getAttributes().get(LDAPConstants.ENTRY_UUID);

            value.setId(attribute.get().toString());
        } catch (NamingException e) {
            throw new IdentityManagementException(e);
        } finally {
            try {
                search.close();
            } catch (NamingException e) {
            }
        }
    }

    @Override
    public void update(IdentityContext context, AttributedType value) {
        //TODO: Implement update
    }

    @Override
    public void remove(IdentityContext context, AttributedType attributedType) {
        this.operationManager.removeEntryById(getConfig().getBaseDN(), attributedType.getId());
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        StringBuffer filter = new StringBuffer("");
        LDAPEntry ldapEntryConfig = identityQuery.getIdentityType().getAnnotation(LDAPEntry.class);

        for (Map.Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            if (IdentityType.ID.equals(entry.getKey())) {
                filter.append(ldapEntryConfig.id()).append(LDAPConstants.COMMA).append(entry.getValue()[0]);
            } else {
                if (AttributeParameter.class.isInstance(entry.getKey())) {
                    AttributeParameter attributeParameter = (AttributeParameter) entry.getKey();

                    String attributeParameterName = attributeParameter.getName();

                    Property<Serializable> property = null;

                    try {
                        property = PropertyQueries.<Serializable>createQuery(identityQuery.getIdentityType())
                                .addCriteria(new NamedPropertyCriteria(attributeParameterName))
                                .getSingleResult();
                    } catch (RuntimeException re) {
                    }

                    Object[] queryParameterValues = entry.getValue();

                    if (property != null && property.getName().equals(attributeParameterName)) {
                        AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);

                        if (attributeProperty != null && !isNullOrEmpty(attributeProperty.mappedName())) {
                            filter.append("(").append(attributeProperty.mappedName()).append(LDAPConstants.EQUAL).append(queryParameterValues[0]).append(")");
                        }
                    }
                }
            }
        }

        NamingEnumeration<SearchResult> answer = null;
        List<V> results = new ArrayList<V>();

        try {
            String baseDN = getConfig().getBaseDN();

            if (ldapEntryConfig != null) {
                baseDN = ldapEntryConfig.baseDN();
            }

            answer = this.operationManager.search(baseDN, filter.toString());

            while (answer.hasMore()) {
                SearchResult searchResult = answer.next();
                String nameInNamespace = searchResult.getNameInNamespace();
                String entryDN = nameInNamespace.substring(nameInNamespace.indexOf(LDAPConstants.COMMA) + 1);

                V attributedType = (V) getConfig().getSupportedTypeByBaseDN(entryDN).newInstance();

                NamingEnumeration<? extends javax.naming.directory.Attribute> ldapAttributes = searchResult.getAttributes().getAll();

                while (ldapAttributes.hasMore()) {
                    javax.naming.directory.Attribute ldapAttribute = ldapAttributes.next();

                    if (ldapAttribute.getID().equals(LDAPConstants.ENTRY_UUID)) {
                        attributedType.setId(ldapAttribute.get().toString());
                    }

                    List<Property<String>> properties = PropertyQueries
                            .<String>createQuery(attributedType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                    for (Property<String> property : properties) {
                        AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);

                        if (attributeProperty.mappedName().equals(ldapAttribute.getID())) {
                            property.setValue(attributedType, ldapAttribute.get().toString());
                        }
                    }
                }

                results.add(attributedType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return results;
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return null;  //TODO: Implement fetchQueryResults
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType type, Attribute<? extends Serializable> attribute) {
        //TODO: Implement setAttribute
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType type, String attributeName) {
        return null;  //TODO: Implement getAttribute
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType type, String attributeName) {
        //TODO: Implement removeAttribute
    }

    @Override
    public void validateCredentials(IdentityContext context, Credentials credentials) {
        //TODO: Implement validateCredentials
    }

    @Override
    public void updateCredential(IdentityContext context, Account account, Object credential, Date effectiveDate, Date expiryDate) {
        //TODO: Implement updateCredential
    }
}