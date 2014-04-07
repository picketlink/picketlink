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

import org.picketlink.common.constants.LDAPConstants;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
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
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Map.Entry;
import static org.picketlink.common.constants.LDAPConstants.COMMA;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EQUAL;
import static org.picketlink.common.constants.LDAPConstants.GROUP_OF_ENTRIES;
import static org.picketlink.common.constants.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.common.constants.LDAPConstants.MEMBER;
import static org.picketlink.common.constants.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;
import static org.picketlink.common.reflection.Reflections.newInstance;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMInternalLog.LDAP_STORE_LOGGER;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.ldap.internal.LDAPUtil.formatDate;
import static org.picketlink.idm.ldap.internal.LDAPUtil.parseDate;

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

    public static final String EMPTY_ATTRIBUTE_VALUE = " ";
    private LDAPOperationManager operationManager;

    @Override
    public void setup(LDAPIdentityStoreConfiguration config) {
        super.setup(config);

        if (config.isActiveDirectory()) {
            LDAP_STORE_LOGGER.ldapActiveDirectoryConfiguration();
        }

        try {
            this.operationManager = new LDAPOperationManager(getConfig());
        } catch (NamingException e) {
            throw MESSAGES.storeLdapCouldNotCreateContext(e);
        }
    }

    @Override
    public void addAttributedType(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            addRelationship((Relationship) attributedType);
        } else {
            this.operationManager.createSubContext(getBindingDN(attributedType), extractAttributes(attributedType, true));

            addToParentAsMember(attributedType);

            attributedType.setId(getEntryIdentifier(attributedType));
        }
    }

    @Override
    public void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        // this store does not support updation of relationship types
        if (Relationship.class.isInstance(attributedType)) {
            LDAP_STORE_LOGGER.ldapRelationshipUpdateNotSupported(attributedType);
        } else {
            BasicAttributes updatedAttributes = extractAttributes(attributedType, false);
            NamingEnumeration<Attribute> attributes = updatedAttributes.getAll();

            try {
                while (attributes.hasMore()) {
                    this.operationManager.modifyAttribute(getBindingDN(attributedType), attributes.next());
                }
            } catch (NamingException ne) {
                throw new IdentityManagementException("Could not update attributes.", ne);
            }
        }
    }

    @Override
    public void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        if (Relationship.class.isInstance(attributedType)) {
            removeRelationship((Relationship) attributedType);
        } else {
            this.operationManager.removeEntryById(getBaseDN(attributedType), attributedType.getId());
        }
    }

    @Override
    protected void removeFromRelationships(final IdentityContext context, final IdentityType identityType) {
        String bindingDN = getBindingDN(identityType);

        for (LDAPMappingConfiguration relationshipConfig : getConfig().getRelationshipConfigs()) {
            for (String attributeName : relationshipConfig.getMappedProperties().values()) {
                StringBuilder filter = new StringBuilder();

                filter.append("(&(").append(attributeName).append(EQUAL).append("").append(bindingDN).append("))");

                try {
                    List<SearchResult> search = this.operationManager.search(getMappingConfig(relationshipConfig.getRelatedAttributedType()).getBaseDN(), filter.toString(), getMappingConfig(relationshipConfig.getRelatedAttributedType()));

                    for (SearchResult result : search) {
                        Attributes attributes = result.getAttributes();
                        Attribute relationshipAttribute = attributes.get(attributeName);

                        if (relationshipAttribute != null && relationshipAttribute.contains(bindingDN)) {
                            relationshipAttribute.remove(bindingDN);

                            if (relationshipAttribute.size() == 0) {
                                relationshipAttribute.add(EMPTY_ATTRIBUTE_VALUE);
                            }

                            this.operationManager.modifyAttribute(result.getNameInNamespace(), relationshipAttribute);
                        }
                    }
                } catch (NamingException e) {
                    throw new IdentityManagementException("Could not remove " + identityType + " from relationship " + relationshipConfig.getMappedClass(), e);
                }
            }
        }
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> results = new ArrayList<V>();

        try {
            if (identityQuery.getParameter(IdentityType.ID) != null) {
                Object[] queryParameterValues = identityQuery.getParameter(IdentityType.ID);
                SearchResult search = this.operationManager.lookupById(getConfig().getBaseDN(), queryParameterValues[0].toString(), null);

                if (search != null) {
                    results.add((V) populateAttributedType(search, null));
                }

                return results;
            } else if (!IdentityType.class.equals(identityQuery.getIdentityType())) {
                // the ldap store does not support queries based on root types. Except if based on the identifier.
                LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(identityQuery.getIdentityType());
                StringBuilder filter = createIdentityTypeSearchFilter(identityQuery, ldapEntryConfig);

                if (filter.length() != 0) {
                    List<SearchResult> search = this.operationManager.search(getBaseDN(ldapEntryConfig), filter.toString(), ldapEntryConfig);

                    for (SearchResult result : search) {
                        results.add((V) populateAttributedType(result, null));
                    }
                }
            }
        } catch (Exception e) {
            throw MESSAGES.queryIdentityTypeFailed(identityQuery, e);
        }

        return results;
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        List<V> results = new ArrayList<V>();

        if (Relationship.class.equals(query.getRelationshipClass())) {
            for (LDAPMappingConfiguration configuration : getConfig().getRelationshipConfigs()) {
                results.addAll(fetchRelationships(query, configuration));
            }
        } else {
            results.addAll(fetchRelationships(query, getMappingConfig(query.getRelationshipClass())));
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

    private <V extends Relationship> List<V> fetchRelationships(final RelationshipQuery<V> query, final LDAPMappingConfiguration mappingConfig) {
        List<V> results = new ArrayList<V>();
        Class<V> relationshipClass = (Class<V>) mappingConfig.getMappedClass();
        Map<QueryParameter, Object[]> parameters = query.getParameters();
        LDAPMappingConfiguration relatedTypeConfig = getMappingConfig(mappingConfig.getRelatedAttributedType());
        StringBuilder filter = new StringBuilder();

        filter.append("(&").append(getObjectClassesFilter(relatedTypeConfig));

        List<String> entriesToFilter = new ArrayList<String>();

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

                if (!getConfig().supportsType(attributedType.getClass(), IdentityStoreConfiguration.IdentityOperation.read)) {
                    return results;
                }

                String bindingDN = null;
                SearchResult result = this.operationManager.lookupById(getBaseDN(attributedType), attributedType.getId(), getMappingConfig(attributedType.getClass()));

                if (result != null) {
                    bindingDN = result.getNameInNamespace();

                    if (!attributedType.getClass().equals(relatedTypeConfig.getMappedClass())) {
                        entriesToFilter.add(bindingDN);
                    }
                }

                boolean filterByOwner = attributedType.getClass().equals(relatedTypeConfig.getMappedClass());

                if (attributeName != null) {
                    Property<IdentityType> property = PropertyQueries
                            .<IdentityType>createQuery(relationshipClass)
                            .addCriteria(new NamedPropertyCriteria(attributeName))
                            .getFirstResult();

                    if (property != null) {
                        filterByOwner = property.getJavaClass().equals(relatedTypeConfig.getMappedClass());
                    }
                }

                if (filterByOwner) {
                    filter.append(this.operationManager.getFilterById(getBaseDN(attributedType), attributedType.getId()));
                } else {
                    filter.append("(").append(attributeName).append(EQUAL).append(bindingDN).append(")");
                }
            }
        }

        filter.append(")");

        try {
            if (filter.length() > 0) {
                String baseDN = getBaseDN(relatedTypeConfig);

                if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                    LDAP_STORE_LOGGER.tracef("Search relationships for type [%s] using filter [%] and baseDN [%s]", relationshipClass, filter.toString(), baseDN);
                }

                List<SearchResult> search = this.operationManager.search(baseDN, filter.toString(), relatedTypeConfig);

                for (SearchResult entry : search) {
                    if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                        LDAP_STORE_LOGGER.tracef("Found entry [%s] for relationship ", entry.getNameInNamespace(), relationshipClass);
                    }

                    Attributes ownerAttributes = entry.getAttributes();
                    AttributedType ownerType = populateAttributedType(entry, null);

                    for (Entry<String, String> memberAttribute : mappingConfig.getMappedProperties().entrySet()) {
                        String attributeName = memberAttribute.getValue();
                        Attribute attribute = ownerAttributes.get(attributeName);
                        NamingEnumeration<?> attributeValues = attribute.getAll();

                        while (attributeValues.hasMore()) {
                            String attributeValue = attributeValues.next().toString();

                            if (!entriesToFilter.isEmpty() && !entriesToFilter.contains(attributeValue)) {
                                continue;
                            }

                            if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                                LDAP_STORE_LOGGER.tracef("Processing relationship [%s] from attribute [%s] with attributeValue [%s]", relationshipClass, attributeName, attributeValue);
                            }

                            if (!isNullOrEmpty(attributeValue.trim())) {
                                Property<AttributedType> associatedProperty = PropertyQueries
                                        .<AttributedType>createQuery(relationshipClass)
                                        .addCriteria(new NamedPropertyCriteria(memberAttribute.getKey()))
                                        .getSingleResult();

                                String memberBaseDN = attributeValue.substring(attributeValue.indexOf(",") + 1);
                                String dn = attributeValue.substring(0, attributeValue.indexOf(","));

                                List<SearchResult> result = this.operationManager.search(memberBaseDN, dn, null);

                                if (result.isEmpty()) {
                                    throw new IdentityManagementException("Associated entry does not exists [" + attributeValue + "].");
                                }

                                Property<AttributedType> property = PropertyQueries
                                        .<AttributedType>createQuery(relationshipClass)
                                        .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                                        .getSingleResult();

                                if (property.getJavaClass().isAssignableFrom(ownerType.getClass())) {
                                    V relationship = newInstance(relationshipClass);

                                    property.setValue(relationship, ownerType);

                                    SearchResult member = result.get(0);

                                    AttributedType relType = populateAttributedType(member, null);

                                    if (associatedProperty.getJavaClass().isAssignableFrom(relType.getClass())) {
                                        associatedProperty.setValue(relationship, relType);

                                        if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                                            LDAP_STORE_LOGGER.tracef("Relationship [%s] created from attribute [%s] with attributeValue [%s]", relationshipClass, attributeName, attributeValue);
                                        }

                                        results.add(relationship);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw MESSAGES.queryRelationshipFailed(query, e);
        }

        return results;
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        //no-op. operation no supported by this store
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account
            account, Class<T> storageClass) {
        //no-op. operation no supported by this store
        return null;
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account
            account, Class<T> storageClass) {
        //no-op. operation no supported by this store
        return null;
    }

    @Override
    protected void removeCredentials(final IdentityContext context, final Account account) {
        //no-op
    }

    private String getBaseDN(final LDAPMappingConfiguration ldapEntryConfig) {
        String baseDN = getConfig().getBaseDN();

        if (ldapEntryConfig.getBaseDN() != null) {
            baseDN = ldapEntryConfig.getBaseDN();
        }

        return baseDN;
    }

    private <V extends IdentityType> StringBuilder createIdentityTypeSearchFilter(final IdentityQuery<V> identityQuery, final LDAPMappingConfiguration ldapEntryConfig) {
        StringBuilder filter = new StringBuilder();

        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            QueryParameter queryParameter = entry.getKey();

            if (!IdentityType.ID.equals(queryParameter)) {
                Object[] queryParameterValues = entry.getValue();

                if (queryParameterValues.length > 0) {

                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                        String attributeName = ldapEntryConfig.getMappedProperties().get(attributeParameter.getName());

                        if (attributeName != null) {
                            Object attributeValue = queryParameterValues[0];

                            if (Date.class.isInstance(attributeValue)) {
                                attributeValue = formatDate((Date) attributeValue);
                            }

                            if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                                filter.append("(").append(attributeName).append(">=").append(attributeValue).append(")");
                            } else if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                                filter.append("(").append(attributeName).append("<=").append(attributeValue).append(")");
                            } else {
                                filter.append("(").append(attributeName).append(LDAPConstants.EQUAL).append(attributeValue).append(")");
                            }
                        }
                    }
                }
            }
        }

        if (filter.length() != 0) {
            filter.insert(0, "(&(");

            if (ldapEntryConfig != null) {
                filter.append(getObjectClassesFilter(ldapEntryConfig));
            } else {
                filter.append("(").append(OBJECT_CLASS).append(EQUAL).append("*").append(")");
            }

            filter.append("))");
        }

        return filter;
    }

    private StringBuilder getObjectClassesFilter(final LDAPMappingConfiguration ldapEntryConfig) {
        StringBuilder builder = new StringBuilder();

        for (String objectClass : ldapEntryConfig.getObjectClasses()) {
            builder.append("(objectClass=").append(objectClass).append(")");
        }

        return builder;
    }

    private void addRelationship(Relationship relationship) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());
        AttributedType ownerType = getRelationshipOwner(relationship);
        Attributes entryAttributes = this.operationManager.getAttributes(ownerType.getId(), getBaseDN(ownerType), mappingConfig);

        for (String relationshipTypeProperty : mappingConfig.getMappedProperties().keySet()) {
            Property<AttributedType> relationshipProperty = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new NamedPropertyCriteria(relationshipTypeProperty))
                    .getSingleResult();

            Attribute attribute = entryAttributes.get(mappingConfig.getMappedProperties().get(relationshipTypeProperty));

            if (attribute != null) {
                List<String> membersToRemove = new ArrayList<String>();
                String memberDN = getBindingDN(relationshipProperty.getValue(relationship));

                try {
                    NamingEnumeration attributeValues = attribute.getAll();

                    while (attributeValues.hasMore()) {
                        Object value = attributeValues.next();

                        if (value.toString().trim().equals(EMPTY_ATTRIBUTE_VALUE.trim())) {
                            membersToRemove.add(EMPTY_ATTRIBUTE_VALUE);
                            membersToRemove.add(EMPTY_ATTRIBUTE_VALUE.trim());
                        }

                        if (value.toString().toLowerCase().equals(memberDN.toLowerCase())) {
                            membersToRemove.add(value.toString());
                        }
                    }

                    for (String memberToRemove : membersToRemove) {
                        attribute.remove(memberToRemove);
                    }
                } catch (NamingException ne) {
                    throw new IdentityManagementException("Could not iterate over members for relationship [" + relationship + "].", ne);
                }

                attribute.add(memberDN);

                this.operationManager.modifyAttribute(getBindingDN(ownerType), attribute);
            }
        }
    }

    private AttributedType getRelationshipOwner(final Relationship relationship) {
        Class<? extends AttributedType> ownertType = getMappingConfig(relationship.getClass()).getRelatedAttributedType();
        Property<AttributedType> property = PropertyQueries
                .<AttributedType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(ownertType))
                .getSingleResult();

        return property.getValue(relationship);
    }

    private void removeRelationship(final Relationship relationship) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());
        AttributedType ownerType = getRelationshipOwner(relationship);
        Attributes ownerAttributes = this.operationManager.getAttributes(ownerType.getId(), getBaseDN(ownerType), mappingConfig);

        for (String typeProperty : mappingConfig.getMappedProperties().keySet()) {
            Property<AttributedType> relProperty = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new NamedPropertyCriteria(typeProperty))
                    .getSingleResult();

            Attribute mappedAttribute = ownerAttributes.get(mappingConfig.getMappedProperties().get(typeProperty));

            if (mappedAttribute != null) {
                String childDN = getBindingDN(relProperty.getValue(relationship));

                if (mappedAttribute.contains(childDN)) {
                    mappedAttribute.remove(childDN);
                }
            }

            if (mappedAttribute.size() == 0) {
                mappedAttribute.add(EMPTY_ATTRIBUTE_VALUE);
            }

            this.operationManager.modifyAttribute(getBindingDN(ownerType), mappedAttribute);
        }
    }

    private AttributedType populateAttributedType(SearchResult searchResult, AttributedType attributedType) {
        return populateAttributedType(searchResult, attributedType, 0);
    }

    private AttributedType populateAttributedType(SearchResult searchResult, AttributedType attributedType, int hierarchyDepthCount) {
        try {
            String entryDN = searchResult.getNameInNamespace();
            String entryBaseDN = entryDN.substring(entryDN.indexOf(COMMA) + 1);
            Attributes attributes = searchResult.getAttributes();

            if (attributedType == null) {
                attributedType = newInstance(getConfig().getSupportedTypeByBaseDN(entryBaseDN, getEntryObjectClasses(attributes)));
            }

            LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());

            if (hierarchyDepthCount > mappingConfig.getHierarchySearchDepth()) {
                return null;
            }

            if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                LDAP_STORE_LOGGER.tracef("Populating attributed type [%s] from DN [%s]", attributedType, entryDN);
            }

            NamingEnumeration<? extends Attribute> ldapAttributes = attributes.getAll();

            while (ldapAttributes.hasMore()) {
                Attribute ldapAttribute = ldapAttributes.next();
                Object attributeValue;

                try {
                    attributeValue = ldapAttribute.get();
                } catch (NoSuchElementException nsee) {
                    continue;
                }

                String attributeName = ldapAttribute.getID();

                if (attributeName.toLowerCase().equals(getConfig().getUniqueIdentifierAttributeName().toLowerCase())) {
                    attributedType.setId(this.operationManager.decodeEntryUUID(attributeValue));
                } else {
                    List<Property<Object>> properties = PropertyQueries
                            .createQuery(attributedType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                    for (Property property : properties) {
                        String ldapAttributeName = mappingConfig.getMappedProperties().get(property.getName());

                        if (ldapAttributeName != null && ldapAttributeName.toLowerCase().equals(attributeName.toLowerCase())) {
                            if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                                LDAP_STORE_LOGGER.tracef("Populating property [%s] from ldap attribute [%s] with value [%s] from DN [%s].", property.getName(), attributeName, attributeValue, entryBaseDN);
                            }

                            if (property.getJavaClass().equals(Date.class)) {
                                property.setValue(attributedType, parseDate(attributeValue.toString()));
                            } else {
                                property.setValue(attributedType, attributeValue);
                            }
                        }
                    }
                }
            }

            if (IdentityType.class.isInstance(attributedType)) {
                IdentityType identityType = (IdentityType) attributedType;

                String createdTimestamp = attributes.get(CREATE_TIMESTAMP).get().toString();

                identityType.setCreatedDate(parseDate(createdTimestamp));
            }

            LDAPMappingConfiguration entryConfig = getMappingConfig(attributedType.getClass());

            if (mappingConfig.getParentMembershipAttributeName() != null) {
                StringBuilder filter = new StringBuilder("(&");

                filter.append("(").append(getObjectClassesFilter(entryConfig)).append(")").append("(").append(mappingConfig.getParentMembershipAttributeName()).append(EQUAL).append("").append(getBindingName(attributedType)).append(COMMA).append(entryBaseDN).append(")");

                filter.append(")");

                if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                    LDAP_STORE_LOGGER.tracef("Searching parent entry for DN [%s] using filter [%s].", entryDN, filter.toString());
                }

                List<SearchResult> search = this.operationManager.search(getConfig().getBaseDN(), filter.toString(), entryConfig);

                if (!search.isEmpty()) {
                    SearchResult next = search.get(0);

                    Property<AttributedType> parentProperty = PropertyQueries
                            .<AttributedType>createQuery(attributedType.getClass())
                            .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                    if (parentProperty != null) {
                        String parentDN = next.getNameInNamespace();
                        String parentBaseDN = parentDN.substring(parentDN.indexOf(",") + 1);
                        Class<? extends AttributedType> baseDNType = getConfig().getSupportedTypeByBaseDN(parentBaseDN, getEntryObjectClasses(attributes));

                        if (parentProperty.getJavaClass().isAssignableFrom(baseDNType)) {
                            if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                                LDAP_STORE_LOGGER.tracef("Found parent [%s] for entry for DN [%s].", parentDN, entryDN);
                            }

                            int hierarchyDepthCount1 = ++hierarchyDepthCount;

                            parentProperty.setValue(attributedType, populateAttributedType(next, null, hierarchyDepthCount1));
                        }
                    }
                } else {
                    if (LDAP_STORE_LOGGER.isTraceEnabled()) {
                        LDAP_STORE_LOGGER.tracef("No parent entry found for DN [%s] using filter [%s].", entryDN, filter.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Could not populate attribute type " + attributedType + ".", e);
        }

        return attributedType;
    }

    private List<String> getEntryObjectClasses(final Attributes attributes) throws NamingException {
        Attribute objectClassesAttribute = attributes.get(OBJECT_CLASS);
        List<String> objectClasses = new ArrayList<String>();

        if (objectClassesAttribute == null) {
            return objectClasses;
        }

        NamingEnumeration<?> all = objectClassesAttribute.getAll();

        while (all.hasMore()) {
            objectClasses.add(all.next().toString());
        }

        return objectClasses;
    }

    private String getBindingName(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType);
    }

    private BasicAttributes extractAttributes(AttributedType attributedType, boolean extractObjectClasses) {
        BasicAttributes entryAttributes = new BasicAttributes();
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
                        propertyValue = EMPTY_ATTRIBUTE_VALUE;
                    }
                }

                entryAttributes.put(mappedProperties.get(propertyName), propertyValue);
            }
        }

        if (extractObjectClasses) {
            LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(attributedType.getClass());

            BasicAttribute objectClassAttribute = new BasicAttribute(OBJECT_CLASS);

            for (String objectClassValue : ldapEntryConfig.getObjectClasses()) {
                objectClassAttribute.add(objectClassValue);

                if (objectClassValue.equals(GROUP_OF_NAMES)
                        || objectClassValue.equals(GROUP_OF_ENTRIES)
                        || objectClassValue.equals(LDAPConstants.GROUP_OF_UNIQUE_NAMES)) {
                    entryAttributes.put(MEMBER, EMPTY_ATTRIBUTE_VALUE);
                }
            }

            entryAttributes.put(objectClassAttribute);
        }

        return entryAttributes;
    }

    private LDAPMappingConfiguration getMappingConfig(Class<? extends AttributedType> attributedType) {
        LDAPMappingConfiguration mappingConfig = getConfig().getMappingConfig(attributedType);

        if (mappingConfig == null) {
            throw new IdentityManagementException("Not mapped type [" + attributedType + "].");
        }

        return mappingConfig;
    }

    LDAPOperationManager getOperationManager() {
        return this.operationManager;
    }

    String getBindingDN(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        Property<String> idProperty = mappingConfig.getIdProperty();

        String baseDN;

        if (mappingConfig.getBaseDN() == null) {
            baseDN = "";
        } else {
            baseDN = COMMA + getBaseDN(attributedType);
        }

        return mappingConfig.getMappedProperties().get(idProperty.getName()) + EQUAL + idProperty.getValue(attributedType) + baseDN;
    }

    private String getBaseDN(AttributedType attributedType) {
        LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
        String baseDN = mappingConfig.getBaseDN();
        String parentDN = mappingConfig.getParentMapping().get(mappingConfig.getIdProperty().getValue(attributedType));

        if (parentDN != null) {
            baseDN = parentDN;
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

        if (baseDN == null) {
            baseDN = getConfig().getBaseDN();
        }

        return baseDN;
    }

    private void addToParentAsMember(final AttributedType attributedType) {
        LDAPMappingConfiguration entryConfig = getMappingConfig(attributedType.getClass());

        if (entryConfig.getParentMembershipAttributeName() != null) {
            Property<AttributedType> parentProperty = PropertyQueries
                    .<AttributedType>createQuery(attributedType.getClass())
                    .addCriteria(new TypedPropertyCriteria(attributedType.getClass()))
                    .getFirstResult();

            if (parentProperty != null) {
                AttributedType parentType = parentProperty.getValue(attributedType);

                if (parentType != null) {
                    Attributes attributes = this.operationManager.getAttributes(parentType.getId(), getBaseDN(parentType), entryConfig);
                    Attribute attribute = attributes.get(entryConfig.getParentMembershipAttributeName());

                    attribute.add(getBindingDN(attributedType));

                    this.operationManager.modifyAttribute(getBindingDN(parentType), attribute);
                }
            }
        }
    }

    private String getEntryIdentifier(final AttributedType attributedType) {
        try {
            // we need this to retrieve the entry's identifier from the ldap server
            List<SearchResult> search = this.operationManager.search(getBaseDN(attributedType), "(" + getBindingName(attributedType) + ")", getMappingConfig(attributedType.getClass()));
            Attribute id = search.get(0).getAttributes().get(getConfig().getUniqueIdentifierAttributeName());

            if (id == null) {
                throw new IdentityManagementException("Could not retrieve identifier for entry [" + getBindingDN(attributedType) + "].");
            }

            return id.get().toString();
        } catch (NamingException ne) {
            throw new IdentityManagementException("Could not add type [" + attributedType + "].", ne);
        }
    }

}