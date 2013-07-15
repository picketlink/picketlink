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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPMappingConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.spi.IdentityContext;
import static java.util.Map.Entry;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.COMMA;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EQUAL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;

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

    private static SimpleDateFormat dateFormat;

    {
        dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

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
    public void add(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;
            LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());

            Property<AttributedType> property = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType(), true)).getSingleResult();

            AttributedType relationalAttributedType = property.getValue(relationship);

            NamingEnumeration<SearchResult> search = null;

            try {
                search = lookupEntryByID(relationalAttributedType.getId(), getMappingConfig(relationalAttributedType.getClass()).getBaseDN());

                Attributes attributes = search.next().getAttributes();

                for (Entry<String, String> entry : mappingConfig.getMappedProperties().entrySet()) {
                    Property<AttributedType> relProperty = PropertyQueries
                            .<AttributedType>createQuery(relationship.getClass())
                            .addCriteria(new NamedPropertyCriteria(entry.getKey())).getSingleResult();

                    AttributedType relType = relProperty.getValue(relationship);

                    javax.naming.directory.Attribute attribute = attributes.get(entry.getValue());

                    if (attribute == null) {
                        attribute = new BasicAttribute(entry.getValue());
                        attributes.put(attribute);
                    }

                    attribute.add(getBindingDN(relType));

                    this.operationManager.modifyAttribute(getBindingDN(relationalAttributedType), attribute);
                }
            } catch (NamingException e) {
                throw IDMMessages.MESSAGES.relationshipAddFailed(relationship, e);
            }
        } else {
            BasicAttributes ldapAttributes = extractAttributes(attributedType);

            BasicAttribute objectClassAttribute = new BasicAttribute(OBJECT_CLASS);

            LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(attributedType.getClass());

            for (String objectClassValue : ldapEntryConfig.getObjectClasses()) {
                objectClassAttribute.add(objectClassValue);
            }

            ldapAttributes.put(objectClassAttribute);

            if (ldapEntryConfig.getObjectClasses().contains(GROUP_OF_NAMES)) {
                ldapAttributes.put(MEMBER, SPACE_STRING);
            }

            String baseDN = ldapEntryConfig.getBaseDN();

            this.operationManager.createSubContext(getBindingName(attributedType) + COMMA + baseDN, ldapAttributes);
            updateCustomAttributes(attributedType);

            if (ldapEntryConfig.getParentMembershipAttributeName() != null) {
                Property<AttributedType> parentProperty = PropertyQueries
                        .<AttributedType>createQuery(attributedType.getClass())
                        .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                if (parentProperty != null) {
                    AttributedType parentType = parentProperty.getValue(attributedType);

                    if (parentType != null) {
                        NamingEnumeration<SearchResult> search = null;

                        try {
                            search = lookupEntryByID(parentType.getId(), ldapEntryConfig.getBaseDN());

                            if (search.hasMore()) {
                                SearchResult next = search.next();

                                javax.naming.directory.Attribute attribute = next.getAttributes().get(ldapEntryConfig.getParentMembershipAttributeName());

                                attribute.add(getBindingDN(attributedType));

                                this.operationManager.modifyAttribute(getBindingDN(parentType), attribute);
                            }
                        } catch (NamingException ne) {
                            throw new IdentityManagementException("Could not add AttributedType [" + attributedType + "].", ne);
                        } finally {
                            if (search != null) {
                                try {
                                    search.close();
                                } catch (NamingException e) {
                                }
                            }
                        }
                    }
                }
            }

            NamingEnumeration<SearchResult> search = null;

            try {
                search = this.operationManager.search(baseDN, "(" + getBindingName(attributedType) + ")");
                populateAttributedType(search.next(), attributedType);
            } catch (NamingException ne) {
                throw new IdentityManagementException("Could not add AttributedType [" + attributedType + "].", ne);
            } finally {
                if (search != null) {
                    try {
                        search.close();
                    } catch (NamingException e) {
                    }
                }
            }
        }
    }

    @Override
    public void update(IdentityContext context, AttributedType attributedType) {
        BasicAttributes updatedAttributes = extractAttributes(attributedType);

        NamingEnumeration<javax.naming.directory.Attribute> attributes = updatedAttributes.getAll();

        String bindingDN = getBindingDN(attributedType);

        try {
            while (attributes.hasMore()) {
                this.operationManager.modifyAttribute(bindingDN, attributes.next());
            }
        } catch (NamingException ne) {
            throw new IdentityManagementException(ne);
        } finally {
            if (attributes != null) {
                try {
                    attributes.close();
                } catch (NamingException e) {
                }
            }
        }

        updateCustomAttributes(attributedType);
    }

    @Override
    public void remove(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;
            LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());

            Property<AttributedType> property = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType(), true)).getSingleResult();

            AttributedType relationalAttributedType = property.getValue(relationship);

            NamingEnumeration<SearchResult> search = null;

            try {
                search = lookupEntryByID(relationalAttributedType.getId(), getMappingConfig(relationalAttributedType.getClass()).getBaseDN());

                Attributes attributes = search.next().getAttributes();

                for (Entry<String, String> entry : mappingConfig.getMappedProperties().entrySet()) {
                    Property<AttributedType> relProperty = PropertyQueries
                            .<AttributedType>createQuery(relationship.getClass())
                            .addCriteria(new NamedPropertyCriteria(entry.getKey())).getSingleResult();

                    AttributedType relType = relProperty.getValue(relationship);

                    javax.naming.directory.Attribute attribute = attributes.get(entry.getValue());

                    if (attribute != null) {
                        String relBindingDN = getBindingDN(relType);

                        if (attribute.contains(relBindingDN)) {
                            attribute.remove(relBindingDN);
                        }
                    }

                    this.operationManager.modifyAttribute(getBindingDN(relationalAttributedType), attribute);
                }
            } catch (NamingException e) {
            }
        } else {
            this.operationManager.removeEntryById(getMappingConfig(attributedType.getClass()).getBaseDN(), attributedType.getId());
        }
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> results = new ArrayList<V>();

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] queryParameterValues = identityQuery.getParameter(IdentityType.ID);
            NamingEnumeration<SearchResult> resultNamingEnumeration = lookupEntryByID(queryParameterValues[0].toString(), getConfig().getBaseDN());

            try {
                while (resultNamingEnumeration.hasMore()) {
                    results.add((V) populateAttributedType(resultNamingEnumeration.next(), null));
                }
            } catch (NamingException ne) {
                throw new IdentityManagementException(ne);
            }

            return results;
        }

        LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(identityQuery.getIdentityType());
        StringBuffer filter = new StringBuffer();

        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            QueryParameter queryParameter = entry.getKey();
            Object[] queryParameterValues = entry.getValue();

            // temporary
            if (queryParameter.equals(Group.PATH)) {
                queryParameter = Group.NAME;
                String path = queryParameterValues[0].toString();
                queryParameterValues = new Object[]{path.substring(path.lastIndexOf("/") + 1)};
            }

            if (queryParameterValues.length > 0) {
                if (IdentityType.ID.equals(queryParameter)) {
                } else {
                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                        String ldapAttributeName = ldapEntryConfig.getMappedProperties().get(attributeParameter.getName());

                        if (ldapAttributeName != null) {
                            filter.append("(").append(ldapAttributeName).append(LDAPConstants.EQUAL).append(queryParameterValues[0]).append(")");
                        }
                    }
                }
            }
        }

        if (filter.length() != 0) {
            NamingEnumeration<SearchResult> search = null;

            try {
                String baseDN = getConfig().getBaseDN();

                if (ldapEntryConfig != null) {
                    baseDN = ldapEntryConfig.getBaseDN();
                }

                search = this.operationManager.search(baseDN, filter.toString());

                while (search.hasMore()) {
                    SearchResult searchResult = search.next();
                    results.add((V) populateAttributedType(searchResult, null));
                }
            } catch (Exception e) {
                throw IDMMessages.MESSAGES.identityTypeQueryFailed(identityQuery, e);
            } finally {
                if (search != null) {
                    try {
                        search.close();
                    } catch (NamingException e) {
                    }
                }
            }
        }

        return results;
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(query.getRelationshipClass());

        Map<QueryParameter, Object[]> parameters = query.getParameters();
        StringBuffer filter = new StringBuffer();
        Map<String, String> namesToMatch = new HashMap<String, String>();

        for (QueryParameter queryParameter : parameters.keySet()) {
            Object[] values = parameters.get(queryParameter);

            if (RelationshipQueryParameter.class.isInstance(queryParameter)) {
                RelationshipQueryParameter relationshipQueryParameter = (RelationshipQueryParameter) queryParameter;

                if (values.length > 0) {
                    AttributedType attributedType = (AttributedType) values[0];
                    String attributeName = mappingConfig.getMappedProperties().get(relationshipQueryParameter.getName());

                    if (attributeName != null) {
                        String bindingDN = getBindingDN(attributedType);

                        namesToMatch.put(bindingDN, relationshipQueryParameter.getName());

                        filter.append("(").append(attributeName).append(EQUAL).append("*").append(bindingDN).append("*)");
                    } else {
                        if (mappingConfig.getRelatedAttributedType().equals(attributedType.getClass())) {
                            filter.append("(").append(getBindingName(attributedType)).append(")");
                        }
                    }
                }
            }
        }

        List<V> results = new ArrayList<V>();

        if (filter.length() > 0) {
            filter.insert(0, "(&").append(")");

            LDAPMappingConfiguration relTypeConfig = getMappingConfig(mappingConfig.getRelatedAttributedType());

            NamingEnumeration<SearchResult> search = null;

            try {
                search = this.operationManager.search(relTypeConfig.getBaseDN(), filter.toString());

                Property<AttributedType> property = PropertyQueries
                        .<AttributedType>createQuery(query.getRelationshipClass())
                        .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType(), true)).getSingleResult();

                while (search.hasMore()) {
                    SearchResult next = search.next();

                    V relationship = query.getRelationshipClass().newInstance();

                    property.setValue(relationship, populateAttributedType(next, null));

                    List<Property<AttributedType>> properties = PropertyQueries
                            .<AttributedType>createQuery(query.getRelationshipClass())
                            .addCriteria(new TypedPropertyCriteria(AttributedType.class, true)).getResultList();

                    for (Property<AttributedType> relProperty : properties) {
                        String attributeName = mappingConfig.getMappedProperties().get(relProperty.getName());

                        if (attributeName != null) {
                            javax.naming.directory.Attribute attribute = next.getAttributes().get(attributeName);
                            NamingEnumeration<?> attributeValues = attribute.getAll();

                            while (attributeValues.hasMore()) {
                                String value = attributeValues.next().toString();

                                if (!isNullOrEmpty(value.trim())) {
                                    String propertyName = namesToMatch.get(value);

                                    if (propertyName != null) {
                                        Property<AttributedType> associatedProperty = PropertyQueries
                                                .<AttributedType>createQuery(query.getRelationshipClass())
                                                .addCriteria(new NamedPropertyCriteria(propertyName)).getSingleResult();

                                        String baseDN = value.substring(value.indexOf(",") + 1);
                                        String dn = value.substring(0, value.indexOf(","));

                                        NamingEnumeration<SearchResult> result = this.operationManager.search(baseDN, dn);

                                        if (!result.hasMore()) {
                                            throw new IdentityManagementException("Associated entry does not exists [" + value + "].");
                                        }

                                        associatedProperty.setValue(relationship, populateAttributedType(result.next(), null));
                                    }
                                }
                            }
                        }
                    }

                    results.add(relationship);
                }
            } catch (Exception e) {
                throw IDMMessages.MESSAGES.relationshipQueryFailed(query, e);
            }

        }

        return results;
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

    private AttributedType populateAttributedType(SearchResult searchResult, AttributedType attributedType) {
        try {
            String nameInNamespace = searchResult.getNameInNamespace();
            String entryDN = nameInNamespace.substring(nameInNamespace.indexOf(COMMA) + 1);
            Attributes attributes = searchResult.getAttributes();

            if (attributedType == null) {
                attributedType = getConfig().getSupportedTypeByBaseDN(entryDN).newInstance();
            }

            LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
            NamingEnumeration<? extends javax.naming.directory.Attribute> ldapAttributes = attributes.getAll();

            while (ldapAttributes.hasMore()) {
                javax.naming.directory.Attribute ldapAttribute = ldapAttributes.next();

                if (ldapAttribute.getID().equals(LDAPConstants.ENTRY_UUID)) {
                    attributedType.setId(ldapAttribute.get().toString());
                }

                List<Property<String>> properties = PropertyQueries
                        .<String>createQuery(attributedType.getClass())
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                for (Property<String> property : properties) {
                    String ldapAttributeName = mappingConfig.getMappedProperties().get(property.getName());

                    if (ldapAttributeName != null && ldapAttributeName.equals(ldapAttribute.getID())) {
                        property.setValue(attributedType, ldapAttribute.get().toString());
                    }
                }
            }

            if (IdentityType.class.isInstance(attributedType)) {
                IdentityType identityType = (IdentityType) attributedType;

                String createdTimestamp = attributes.get(CREATE_TIMESTAMP).get().toString();
                long timeAdjust = 11644473600000L;  // adjust factor for converting it to java

                identityType.setCreatedDate(
                        dateFormat.parse(String.valueOf(Long.parseLong(createdTimestamp.substring(0, createdTimestamp.indexOf('Z'))) / 10000 - timeAdjust)));

                identityType.setPartition(new Realm(Realm.DEFAULT_REALM));
            }

            if (mappingConfig.getParentMembershipAttributeName() != null) {
                StringBuffer filter = new StringBuffer("(" + mappingConfig.getParentMembershipAttributeName() + EQUAL + "*" + getBindingDN(attributedType) + "*)");

                NamingEnumeration<SearchResult> search = this.operationManager.search(mappingConfig.getBaseDN(), filter.toString());

                while (search.hasMore()) {
                    SearchResult next = search.next();

                    Property<AttributedType> parentProperty = PropertyQueries
                            .<AttributedType>createQuery(attributedType.getClass())
                            .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                    if (parentProperty != null) {
                        parentProperty.setValue(attributedType, populateAttributedType(next, null));
                    }
                }
            }

            populateCustomAttributes(attributedType);
        } catch (NamingException e) {
            throw new IdentityManagementException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attributedType;
    }

    private String getBindingName(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType);
    }

    private String getBindingDN(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType) + COMMA + mappingConfig.getBaseDN();
    }

    private BasicAttributes extractAttributes(AttributedType attributedType) {
        BasicAttributes ldapEntryAttributes = new BasicAttributes();

        Map<String, String> mappedProperties = getMappingConfig(attributedType.getClass()).getMappedProperties();

        for (String propertyName : mappedProperties.keySet()) {
            Property<Object> property = PropertyQueries
                    .<Object>createQuery(attributedType.getClass())
                    .addCriteria(new NamedPropertyCriteria(propertyName)).getSingleResult();

            Object propertyValue = property.getValue(attributedType);

            if (AttributedType.class.isInstance(propertyValue)) {
                AttributedType referencedType = (AttributedType) propertyValue;
                propertyValue = getBindingDN(referencedType);
            } else {
                if (propertyValue == null || isNullOrEmpty(propertyValue.toString())) {
                    propertyValue = " ";
                }
            }

            ldapEntryAttributes.put(mappedProperties.get(propertyName), propertyValue);
        }

        return ldapEntryAttributes;
    }

    private void updateCustomAttributes(AttributedType attributedType) {
        String customDN = getCustomAttributesDN(attributedType);

        LDAPCustomAttributes customAttributes = this.operationManager.lookup(customDN);
        boolean exists = customAttributes != null;

        if (!exists) {
            customAttributes = new LDAPCustomAttributes();
        }

        customAttributes.clear();

        for (Attribute attribute : attributedType.getAttributes()) {
            customAttributes.addAttribute(attribute.getName(), attribute.getValue());
        }

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            if (identityType.getExpirationDate() != null) {
                customAttributes.addAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE, String.valueOf(identityType.getExpirationDate().getTime()));
            }

            customAttributes.addAttribute(CUSTOM_ATTRIBUTE_ENABLED, String.valueOf(identityType.isEnabled()));
        }

        if (exists) {
            this.operationManager.rebind(customDN, customAttributes);
        } else {
            this.operationManager.bind(customDN, customAttributes);
        }
    }

    private void populateCustomAttributes(AttributedType attributedType) {
        String customDN = getCustomAttributesDN(attributedType);

        LDAPCustomAttributes customAttributes = this.operationManager.lookup(customDN);

        if (customAttributes != null) {
            for (Entry<String, Serializable> attribute : customAttributes.getAttributes().entrySet()) {
                if (!attribute.getKey().equals(CUSTOM_ATTRIBUTE_ENABLED)
                        && !attribute.getKey().equals(CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                    attributedType.setAttribute(new Attribute<Serializable>(attribute.getKey(), attribute.getValue()));
                }
            }

            if (IdentityType.class.isInstance(attributedType)) {
                IdentityType identityType = (IdentityType) attributedType;

                Object expiryDate = customAttributes.getAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE);

                if (expiryDate != null) {
                    identityType.setExpirationDate(new Date(Long.valueOf(expiryDate.toString())));
                }

                Object enabled = customAttributes.getAttribute(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED);

                if (enabled != null) {
                    identityType.setEnabled(Boolean.valueOf(enabled.toString()));
                }
            }
        }
    }

    private String getCustomAttributesDN(AttributedType attributedType) {
        return CN + "=custom-attributes" + COMMA + getBindingName(attributedType) + COMMA + getMappingConfig(attributedType.getClass()).getBaseDN();
    }

    private LDAPMappingConfiguration getMappingConfig(Class<? extends AttributedType> attributedType) {
        LDAPMappingConfiguration mappingConfig = getConfig().getMappingConfig(attributedType);

        if (mappingConfig == null) {
            throw new IdentityManagementException("Not mapped type [" + attributedType + "].");
        }

        return mappingConfig;
    }

    private NamingEnumeration<SearchResult> lookupEntryByID(String id, String baseDN) {
        return this.operationManager.lookupById(baseDN, id);
    }

}