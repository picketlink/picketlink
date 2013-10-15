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

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.common.util.ClassUtil;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPMappingConfiguration;
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.util.Map.Entry;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.COMMA;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.idm.ldap.internal.LDAPConstants.ENTRY_UUID;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EQUAL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;

/**
 * An IdentityStore implementation backed by an LDAP directory
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({LDAPPlainTextPasswordCredentialHandler.class})
public class LDAPIdentityStore extends AbstractIdentityStore<LDAPIdentityStoreConfiguration>
        implements CredentialStore<LDAPIdentityStoreConfiguration> {

    private LDAPOperationManager operationManager;

    @Override
    public void setup(LDAPIdentityStoreConfiguration config) {
        super.setup(config);

        try {
            this.operationManager = new LDAPOperationManager(getConfig());
        } catch (NamingException e) {
            throw MESSAGES.storeLdapCouldNotCreateContext(e);
        }
    }

    @Override
    public void addAttributedType(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            storeRelationship((Relationship) attributedType);
        } else {
            BasicAttributes entryAttributes = extractAttributes(attributedType);

            BasicAttribute objectClassAttribute = new BasicAttribute(OBJECT_CLASS);

            LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(attributedType.getClass());

            for (String objectClassValue : ldapEntryConfig.getObjectClasses()) {
                objectClassAttribute.add(objectClassValue);
            }

            entryAttributes.put(objectClassAttribute);

            if (ldapEntryConfig.getObjectClasses().contains(GROUP_OF_NAMES)) {
                entryAttributes.put(MEMBER, "cn=empty-member," + getConfig().getBaseDN());
            }

            this.operationManager.createSubContext(getBindingDN(attributedType), entryAttributes);

            if (ldapEntryConfig.getParentMembershipAttributeName() != null) {
                Property<AttributedType> parentProperty = PropertyQueries
                        .<AttributedType>createQuery(attributedType.getClass())
                        .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                if (parentProperty != null) {
                    AttributedType parentType = parentProperty.getValue(attributedType);

                    if (parentType != null) {
                        NamingEnumeration<SearchResult> search = null;

                        try {
                            search = lookupEntryByID(parentType.getId(), getBaseDN(parentType));

                            if (search.hasMore()) {
                                SearchResult next = search.next();

                                javax.naming.directory.Attribute attribute = next.getAttributes().get(ldapEntryConfig.getParentMembershipAttributeName());

                                attribute.add(getBindingDN(attributedType));

                                this.operationManager.modifyAttribute(getBindingDN(parentType), attribute);
                            }
                        } catch (NamingException ne) {
                            throw new IdentityManagementException("Could not create parent [" + parentType + "] child [" + attributedType + "] hierarchy.", ne);
                        } finally {
                            safeClose(search);
                        }
                    }
                }
            }

            NamingEnumeration<SearchResult> search = null;

            try {
                search = this.operationManager.search(getBaseDN(attributedType), "(" + getBindingName(attributedType) + ")");
                populateAttributedType(context, search.next(), attributedType);
            } catch (NamingException ne) {
                throw new IdentityManagementException("Could not add AttributedType [" + attributedType + "].", ne);
            } finally {
                safeClose(search);
            }
        }
    }

    @Override
    public void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        // this store does not support updation of relationship types
        if (Relationship.class.isInstance(attributedType)) {
            throw MESSAGES.attributedTypeUnsupportedOperation(attributedType.getClass(),
                    IdentityOperation.update, attributedType.getClass(), IdentityOperation.update);
        }

        BasicAttributes updatedAttributes = extractAttributes(attributedType);

        NamingEnumeration<javax.naming.directory.Attribute> attributes = updatedAttributes.getAll();

        String bindingDN = getBindingDN(attributedType);

        try {
            while (attributes.hasMore()) {
                this.operationManager.modifyAttribute(bindingDN, attributes.next());
            }
        } catch (NamingException ne) {
            throw new IdentityManagementException("Could not update attributes.", ne);
        } finally {
            safeClose(attributes);
        }
    }

    @Override
    public void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;
            LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());

            Property<AttributedType> property = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                    .getSingleResult();

            AttributedType relationalAttributedType = property.getValue(relationship);

            NamingEnumeration<SearchResult> search = null;

            try {
                search = lookupEntryByID(relationalAttributedType.getId(), getBaseDN(relationalAttributedType));

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
                throw new IdentityManagementException("Could not remove referenced types from relationship type.", e);
            } finally {
                safeClose(search);
            }
        } else {
            List<LDAPMappingConfiguration> relationshipConfigs = getConfig().getRelationshipConfigs();
            String bindingDN = getBindingDN(attributedType);

            try {
                for (LDAPMappingConfiguration relationshipConfig : relationshipConfigs) {
                    for (String attributeName : relationshipConfig.getMappedProperties().values()) {
                        StringBuilder filter = new StringBuilder();

                        filter.append("(&(").append(attributeName).append(EQUAL).append("").append(bindingDN).append("))");

                        NamingEnumeration<SearchResult> search = this.operationManager.search(getMappingConfig(relationshipConfig.getRelatedAttributedType()).getBaseDN(), filter.toString());

                        while (search.hasMore()) {
                            SearchResult result = search.next();
                            Attributes attributes = result.getAttributes();

                            javax.naming.directory.Attribute relationshipAttribute = attributes.get(attributeName);

                            if (relationshipAttribute != null && relationshipAttribute.contains(bindingDN)) {
                                relationshipAttribute.remove(bindingDN);
                                if (relationshipAttribute.size() == 0) {
                                    relationshipAttribute.add(getEmptyMemberDN());
                                }
                                this.operationManager.modifyAttribute(result.getNameInNamespace(), relationshipAttribute);
                            }
                        }

                        safeClose(search);
                    }
                }
            } catch (NamingException e) {
                throw new IdentityManagementException(e);
            }

            this.operationManager.removeEntryById(getBaseDN(attributedType), attributedType.getId());
        }
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> results = new ArrayList<V>();

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] queryParameterValues = identityQuery.getParameter(IdentityType.ID);
            NamingEnumeration<SearchResult> search = lookupEntryByID(queryParameterValues[0].toString(), getConfig().getBaseDN());

            try {
                while (search.hasMore()) {
                    results.add((V) populateAttributedType(context, search.next(), null));
                }
            } catch (NamingException ne) {
                throw new IdentityManagementException("Could not create identity type from LDAP entry.", ne);
            } finally {
                safeClose(search);
            }

            return results;
        }

        LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(identityQuery.getIdentityType());
        StringBuilder filter = new StringBuilder();

        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            QueryParameter queryParameter = entry.getKey();
            Object[] queryParameterValues = entry.getValue();

            if (queryParameterValues.length > 0) {
                if (!IdentityType.ID.equals(queryParameter)) {
                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                        String ldapAttributeName = ldapEntryConfig.getMappedProperties().get(attributeParameter.getName());

                        if (ldapAttributeName != null) {
                            Object value = queryParameterValues[0];

                            if (Date.class.isInstance(value)) {
                                DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
                                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                                value = formatter.format(((Date) value));
                            }

                            if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                                filter.append("(").append(ldapAttributeName).append(">=").append(value).append(")");
                            } else if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                                filter.append("(").append(ldapAttributeName).append("<=").append(value).append(")");
                            } else {
                                filter.append("(").append(ldapAttributeName).append(LDAPConstants.EQUAL).append(value).append(")");
                            }
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

                filter.insert(0, "(&(");

                for (String objectClass : ldapEntryConfig.getObjectClasses()) {
                    filter.append("(objectClass=").append(objectClass).append(")");
                }

                filter.append("))");

                search = this.operationManager.search(baseDN, filter.toString());

                while (search.hasMoreElements()) {
                    V type = (V) populateAttributedType(context, search.nextElement(), null);

                    if (type != null) {
                        results.add(type);
                    }
                }
            } catch (Exception e) {
                throw new IdentityManagementException("Could not query identity types.", e);
            } finally {
                safeClose(search);
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
        List<V> results = new ArrayList<V>();

        if (Relationship.class.equals(query.getRelationshipClass())) {
            for (LDAPMappingConfiguration configuration : getConfig().getRelationshipConfigs()) {
                results.addAll(fetchRelationships(context, query, configuration));
            }
        } else {
            results.addAll(fetchRelationships(context, query, getMappingConfig(query.getRelationshipClass())));
        }

        return results;
    }

    private String getRelationshipMappedProperty(Class<? extends IdentityType> identityType, LDAPMappingConfiguration mappingConfig) {
        final Property<Object> property = PropertyQueries.createQuery(mappingConfig.getMappedClass()).addCriteria(new TypedPropertyCriteria(identityType, MatchOption.ALL)).getFirstResult();

        if (property == null) {
            return null;
        }

        return mappingConfig.getMappedProperties().get(property.getName());
    }

    private <V extends Relationship> List<V> fetchRelationships(final IdentityContext context, final RelationshipQuery<V> query, final LDAPMappingConfiguration mappingConfig) {
        List<V> results = new ArrayList<V>();
        Class<V> relationshipClass = (Class<V>) mappingConfig.getMappedClass();
        StringBuilder filter = new StringBuilder();
        List<AttributedType> referencedTypes = new ArrayList<AttributedType>();
        Map<QueryParameter, Object[]> parameters = query.getParameters();

        for (QueryParameter queryParameter : parameters.keySet()) {
            Object[] values = parameters.get(queryParameter);
            RelationshipQueryParameter relationshipQueryParameter = null;
            String attributeName = null;

            if (RelationshipQueryParameter.class.isInstance(queryParameter)) {
                relationshipQueryParameter = (RelationshipQueryParameter) queryParameter;
                attributeName = mappingConfig.getMappedProperties().get(relationshipQueryParameter.getName());
            } else if (Relationship.IDENTITY.equals(queryParameter)) {
                IdentityType identityType = (IdentityType) values[0];

                if (!mappingConfig.getRelatedAttributedType().isInstance(identityType)) {
                    attributeName = getRelationshipMappedProperty(identityType.getClass(), mappingConfig);
                }
            } else {
                continue;
            }

            for (Object value : values) {
                AttributedType attributedType = (AttributedType) value;
                if (attributeName != null) {
                    filter.append("(").append(attributeName).append(EQUAL).append("").append(getBindingDN(attributedType)).append(")");
                } else {
                    if (mappingConfig.getRelatedAttributedType().isAssignableFrom(attributedType.getClass())) {
                        referencedTypes.add(attributedType);
                    }
                }
            }
        }

        NamingEnumeration<SearchResult> search = null;

        try {
            if (!referencedTypes.isEmpty()) {
                for (AttributedType relFilter : referencedTypes) {
                    search = this.operationManager.search(getBaseDN(relFilter), getBindingName(relFilter));

                    List<Property<AttributedType>> properties = PropertyQueries
                            .<AttributedType>createQuery(relationshipClass)
                            .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE))
                            .getResultList();
                    Property<AttributedType> rootProperty = PropertyQueries
                            .<AttributedType>createQuery(relationshipClass)
                            .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                            .getSingleResult();

                    while (search.hasMore()) {
                        SearchResult next = search.next();
                        Attributes attributes = next.getAttributes();

                        for (Property<AttributedType> property : properties) {
                            if (!property.getJavaClass().equals(relFilter.getClass())) {
                                String relAttributeName = mappingConfig.getMappedProperties().get(property.getName());
                                javax.naming.directory.Attribute attribute = attributes.get(relAttributeName);

                                for (QueryParameter queryParameter : parameters.keySet()) {
                                    Object[] values = parameters.get(queryParameter);

                                    if (filter.length() > 0) {
                                        for (Object value : values) {
                                            IdentityType relType = (IdentityType) value;

                                            String attributeName = null;

                                            if (RelationshipQueryParameter.class.isInstance(queryParameter)) {
                                                RelationshipQueryParameter relationshipQueryParameter = (RelationshipQueryParameter) queryParameter;
                                                attributeName = relationshipQueryParameter.getName();
                                            } else {
                                                attributeName = getRelationshipMappedProperty(relType.getClass(), mappingConfig);
                                            }

                                            if (attributeName.equals(property.getName())) {
                                                if (!attribute.contains(getBindingDN(relType))) {
                                                    break;
                                                }

                                                V relationship = createRelationshipInstance(relationshipClass);

                                                rootProperty.setValue(relationship, populateAttributedType(context, next, null));
                                                property.setValue(relationship, relType);

                                                results.add(relationship);
                                            }
                                        }
                                    } else {
                                        NamingEnumeration<?> all = attribute.getAll();

                                        while (all.hasMore()) {
                                            String member = all.next().toString();

                                            if (isEmptyMember(member)) {
                                                continue;
                                            }

                                            if (!isNullOrEmpty(member.trim())) {
                                                V relationship = createRelationshipInstance(relationshipClass);

                                                rootProperty.setValue(relationship, populateAttributedType(context, next, null));

                                                String baseDN = member.substring(member.indexOf(",") + 1);
                                                String dn = member.substring(0, member.indexOf(","));

                                                NamingEnumeration<SearchResult> result = this.operationManager.search(baseDN, dn);

                                                if (!result.hasMore()) {
                                                    throw new IdentityManagementException("Associated entry does not exists [" + member + "].");
                                                }

                                                property.setValue(relationship, populateAttributedType(context, result.next(), null));

                                                results.add(relationship);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (filter.length() > 0) {
                    search = this.operationManager.search(getMappingConfig(mappingConfig.getRelatedAttributedType()).getBaseDN(), filter.toString());

                    Property<AttributedType> property = PropertyQueries
                            .<AttributedType>createQuery(relationshipClass)
                            .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                            .getSingleResult();

                    while (search.hasMore()) {
                        SearchResult next = search.next();
                        Attributes attributes = next.getAttributes();

                        for (Entry<String, String> memberAttribute : mappingConfig.getMappedProperties().entrySet()) {
                            javax.naming.directory.Attribute attribute = attributes.get(memberAttribute.getValue());
                            NamingEnumeration<?> attributeValues = attribute.getAll();

                            while (attributeValues.hasMore()) {
                                String value = attributeValues.next().toString();

                                if (isEmptyMember(value)) {
                                    continue;
                                }

                                if (!isNullOrEmpty(value.trim())) {
                                    Property<AttributedType> associatedProperty = PropertyQueries
                                            .<AttributedType>createQuery(relationshipClass)
                                            .addCriteria(new NamedPropertyCriteria(memberAttribute.getKey())).getSingleResult();

                                    String baseDN = value.substring(value.indexOf(",") + 1);
                                    String dn = value.substring(0, value.indexOf(","));

                                    NamingEnumeration<SearchResult> result = this.operationManager.search(baseDN, dn);

                                    if (!result.hasMore()) {
                                        throw new IdentityManagementException("Associated entry does not exists [" + value + "].");
                                    }


                                    AttributedType ownerRelType = populateAttributedType(context, next, null);

                                    if (property.getJavaClass().isAssignableFrom(ownerRelType.getClass())) {
                                        V relationship = createRelationshipInstance(relationshipClass);

                                        property.setValue(relationship, ownerRelType);

                                        SearchResult member = result.next();

                                        AttributedType relType = populateAttributedType(context, member, null);

                                        if (associatedProperty.getJavaClass().isAssignableFrom(relType.getClass())) {
                                            associatedProperty.setValue(relationship, relType);
                                            results.add(relationship);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw IDMMessages.MESSAGES.queryRelationshipFailed(query, e);
        } finally {
            safeClose(search);
        }

        return results;
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        //TODO: Implement storeCredential
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    private void storeRelationship(Relationship relationship) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());

        Property<AttributedType> property = PropertyQueries
                .<AttributedType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                .getSingleResult();

        AttributedType relationalAttributedType = property.getValue(relationship);

        NamingEnumeration<SearchResult> search = null;

        try {
            search = lookupEntryByID(relationalAttributedType.getId(), getBaseDN(relationalAttributedType));

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
            throw new IdentityManagementException("Could not store relationship.", e);
        } finally {
            safeClose(search);
        }
    }

    private AttributedType populateAttributedType(final IdentityContext context, SearchResult searchResult, AttributedType attributedType) {
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
                Object value = ldapAttribute.get();

                if (ldapAttribute.getID().equals(ENTRY_UUID)) {
                    attributedType.setId(value.toString());
                } else {
                    List<Property<Object>> properties = PropertyQueries
                            .createQuery(attributedType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                    for (Property property : properties) {
                        String ldapAttributeName = mappingConfig.getMappedProperties().get(property.getName());

                        if (ldapAttributeName != null && ldapAttributeName.equals(ldapAttribute.getID())) {
                            if (property.getJavaClass().equals(Date.class)) {
                                property.setValue(attributedType, parseLDAPDate(value.toString()));
                            } else {
                                property.setValue(attributedType, value);
                            }
                        }
                    }
                }
            }

            if (IdentityType.class.isInstance(attributedType)) {
                IdentityType identityType = (IdentityType) attributedType;

                String createdTimestamp = attributes.get(CREATE_TIMESTAMP).get().toString();

                identityType.setCreatedDate(parseLDAPDate(createdTimestamp));

                identityType.setPartition(context.getPartition());
            }

            if (mappingConfig.getParentMembershipAttributeName() != null) {
                StringBuilder filter = new StringBuilder("(|");

                filter.append("(").append(mappingConfig.getParentMembershipAttributeName()).append(EQUAL).append("").append(getBindingName(attributedType)).append(COMMA).append(entryDN).append(")");

                filter.append(")");

                NamingEnumeration<SearchResult> search = this.operationManager.search(getConfig().getBaseDN(), filter.toString());

                try {
                    while (search.hasMore()) {
                        SearchResult next = search.next();

                        Property<AttributedType> parentProperty = PropertyQueries
                                .<AttributedType>createQuery(attributedType.getClass())
                                .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                        if (parentProperty != null) {
                            String baseDN = next.getNameInNamespace().substring(next.getNameInNamespace().indexOf(",") + 1);
                            Class<? extends AttributedType> baseDNType = getConfig().getSupportedTypeByBaseDN(baseDN);

                            if (parentProperty.getJavaClass().isAssignableFrom(baseDNType)) {
                                parentProperty.setValue(attributedType, populateAttributedType(context, next, null));
                            }
                        }
                    }
                } finally {
                    safeClose(search);
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Could not populate attribute type.", e);
        }

        return attributedType;
    }

    private String getBindingName(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType);
    }

    private BasicAttributes extractAttributes(AttributedType attributedType) {
        BasicAttributes ldapEntryAttributes = new BasicAttributes();

        Map<String, String> mappedProperties = getMappingConfig(attributedType.getClass()).getMappedProperties();

        for (String propertyName : mappedProperties.keySet()) {
            if (!getMappingConfig(attributedType.getClass()).getReadOnlyAttributes().contains(propertyName)) {
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
        }

        return ldapEntryAttributes;
    }

    private String getCustomAttributesDN(AttributedType attributedType) {
        return CN + "=custom-attributes" + COMMA + getBindingDN(attributedType);
    }

    private LDAPMappingConfiguration getMappingConfig(Class<? extends AttributedType> attributedType) {
        LDAPMappingConfiguration mappingConfig = getConfig().getMappingConfig(attributedType);

        if (mappingConfig == null) {
            throw new IdentityManagementException("Not mapped type [" + attributedType + "].");
        }

        return mappingConfig;
    }

    NamingEnumeration<SearchResult> lookupEntryByID(String id, String baseDN) {
        return this.operationManager.lookupById(baseDN, id);
    }

    LDAPOperationManager getOperationManager() {
        return this.operationManager;
    }

    String getBindingDN(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType) + COMMA + getBaseDN(attributedType);
    }

    private String getBaseDN(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        String baseDN = mappingConfig.getBaseDN();

        String mappingDN = mappingConfig.getParentMapping().get(mappingConfig.getIdProperty().getValue(attributedType));

        if (mappingDN != null) {
            baseDN = mappingDN;
        } else {
            Property<AttributedType> parentProperty = PropertyQueries
                    .<AttributedType>createQuery(attributedType.getClass())
                    .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

            if (parentProperty != null) {
                AttributedType parentType = parentProperty.getValue(attributedType);

                if (parentType != null) {
                    Property<String> parentIdProperty = getMappingConfig(parentType.getClass()).getIdProperty();

                    String parentId = parentIdProperty.getValue(parentType);

                    String parentBaseDN = mappingConfig.getParentMapping().get(parentId);

                    if (parentBaseDN != null) {
                        baseDN = parentBaseDN;
                    } else {
                        baseDN = getBaseDN(parentType);
                    }
                }
            }
        }

        return baseDN;
    }

    /**
     * Parses dates/time stamps stored in LDAP. Some possible values:
     * <p/>
     * <ul> <li>20020228150820</li> <li>20030228150820Z</li> <li>20050228150820.12</li> <li>20060711011740.0Z</li>
     * </ul>
     *
     * @param dateText the date string.
     * @return the Date.
     */
    private Date parseLDAPDate(String dateText) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            if (dateText.endsWith("Z")) {
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                dateFormat.setTimeZone(TimeZone.getDefault());
            }

            return dateFormat.parse(dateText);
        } catch (Exception e) {
            throw new IdentityManagementException("Error converting ldap date.", e);
        }
    }

    private void safeClose(NamingEnumeration<?> search) {
        if (search != null) {
            try {
                search.close();
            } catch (NamingException e) {
            }
        }
    }

    private <V extends Relationship> V createRelationshipInstance(Class<V> relationshipClass) throws InstantiationException, IllegalAccessException {
        return (V) ClassUtil.loadClass(getClass(), relationshipClass.getName()).newInstance();
    }

    private boolean isEmptyMember(final String value) {
        return value.contains(getEmptyMemberDN());
    }

    private String getEmptyMemberDN() {
        return "cn=empty-member," + getConfig().getBaseDN();
    }

}