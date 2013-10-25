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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
            LDAPMappingConfiguration ldapEntryConfig = getMappingConfig(attributedType.getClass());
            String bindingDN = getBindingDN(attributedType);

            this.operationManager.createSubContext(bindingDN, extractAttributes(attributedType, true));

            if (ldapEntryConfig.getParentMembershipAttributeName() != null) {
                Property<AttributedType> parentProperty = PropertyQueries
                        .<AttributedType>createQuery(attributedType.getClass())
                        .addCriteria(new TypedPropertyCriteria(attributedType.getClass()))
                        .getFirstResult();

                if (parentProperty != null) {
                    AttributedType parentType = parentProperty.getValue(attributedType);

                    if (parentType != null) {
                        Attributes attributes = this.operationManager.getAttributes(parentType.getId(), getBaseDN(parentType), ldapEntryConfig);

                        Attribute attribute = attributes.get(ldapEntryConfig.getParentMembershipAttributeName());

                        attribute.add(bindingDN);

                        this.operationManager.modifyAttribute(getBindingDN(parentType), attribute);
                    }
                }
            }

            NamingEnumeration<SearchResult> search = null;

            try {
                search = this.operationManager.search(getBaseDN(attributedType), "(" + getBindingName(attributedType) + ")", ldapEntryConfig);
                populateAttributedType(context, search.next(), attributedType);
            } catch (NamingException ne) {
                throw new IdentityManagementException("Could not load recently created type [" + attributedType + "].", ne);
            } finally {
                safeClose(search);
            }
        }
    }

    @Override
    public void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        // this store does not support updation of relationship types
        if (Relationship.class.isInstance(attributedType)) {
            LDAP_STORE_LOGGER.ldapRelationshipUpdateNotSupported(attributedType);
        } else {
            BasicAttributes updatedAttributes = extractAttributes(attributedType, false);
            String bindingDN = getBindingDN(attributedType);
            NamingEnumeration<javax.naming.directory.Attribute> attributes = updatedAttributes.getAll();

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

                NamingEnumeration<SearchResult> search = null;

                try {
                    search = this.operationManager.search(getMappingConfig(relationshipConfig.getRelatedAttributedType()).getBaseDN(), filter.toString(), getMappingConfig(relationshipConfig.getRelatedAttributedType()));

                    while (search.hasMore()) {
                        SearchResult result = search.next();
                        Attributes attributes = result.getAttributes();

                        Attribute relationshipAttribute = attributes.get(attributeName);

                        if (relationshipAttribute != null && relationshipAttribute.contains(bindingDN)) {
                            relationshipAttribute.remove(bindingDN);
                            if (relationshipAttribute.size() == 0) {
                                relationshipAttribute.add(getEmptyAttributeValue());
                            }
                            this.operationManager.modifyAttribute(result.getNameInNamespace(), relationshipAttribute);
                        }
                    }
                } catch (NamingException e) {
                    throw new IdentityManagementException(e);
                } finally {
                    safeClose(search);
                }
            }
        }
    }

    @Override
    protected void removeCredentials(final IdentityContext context, final Account account) {
        //no-op
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> results = new ArrayList<V>();

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] queryParameterValues = identityQuery.getParameter(IdentityType.ID);
            NamingEnumeration<SearchResult> search = lookupEntryByID(queryParameterValues[0].toString(), getConfig().getBaseDN(), null);

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
        } else if (!IdentityType.class.equals(identityQuery.getIdentityType())) {
            // the ldap store does not support queries based on root types. Except if based on the identifier.
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

                    filter.insert(0, "(&(");

                    if (ldapEntryConfig != null) {
                        if (ldapEntryConfig.getBaseDN() != null) {
                            baseDN = ldapEntryConfig.getBaseDN();
                        }

                        filter.append(getObjectClassesFilter(ldapEntryConfig));
                    } else {
                        filter.append("(objectClass=*)");
                    }

                    filter.append("))");

                    search = this.operationManager.search(baseDN, filter.toString(), ldapEntryConfig);

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
        }

        return results;
    }

    private StringBuilder getObjectClassesFilter(final LDAPMappingConfiguration ldapEntryConfig) {
        StringBuilder builder = new StringBuilder();

        for (String objectClass : ldapEntryConfig.getObjectClasses()) {
            builder.append("(objectClass=").append(objectClass).append(")");
        }

        return builder;
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
        List<AttributedType> referencedTypes = new ArrayList<AttributedType>();
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

                try {
                    NamingEnumeration<SearchResult> result = this.operationManager.lookupById(getBaseDN(attributedType), attributedType.getId(), getMappingConfig(attributedType.getClass()));

                    if (result.hasMore()) {
                        SearchResult next = result.next();

                        bindingDN = next.getNameInNamespace();

                        if (!attributedType.getClass().equals(relatedTypeConfig.getMappedClass())) {
                            entriesToFilter.add(bindingDN);
                        }
                    }
                } catch (NamingException e) {
                    throw new IdentityManagementException(e);
                }

                if (attributedType.getClass().equals(relatedTypeConfig.getMappedClass())) {
                    filter.append(this.operationManager.getFilterById(getBaseDN(attributedType), attributedType.getId()));
                } else {
                    filter.append("(").append(attributeName).append(EQUAL).append(bindingDN).append(")");
                }
            }
        }

        filter.append(")");

        NamingEnumeration<SearchResult> search = null;

        try {
            if (filter.length() > 0) {
                String baseDN = relatedTypeConfig.getBaseDN();

                if (baseDN == null) {
                    baseDN = getConfig().getBaseDN();
                }

                search = this.operationManager.search(baseDN, filter.toString(), relatedTypeConfig);

                Property<AttributedType> property = PropertyQueries
                        .<AttributedType>createQuery(relationshipClass)
                        .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                        .getSingleResult();

                while (search.hasMore()) {
                    SearchResult next = search.next();
                    Attributes attributes = next.getAttributes();
                    AttributedType ownerRelType = populateAttributedType(context, next, null);

                    for (Entry<String, String> memberAttribute : mappingConfig.getMappedProperties().entrySet()) {
                        javax.naming.directory.Attribute attribute = attributes.get(memberAttribute.getValue());
                        NamingEnumeration<?> attributeValues = attribute.getAll();

                        while (attributeValues.hasMore()) {
                            String value = attributeValues.next().toString();

                            if (isEmptyMember(value)) {
                                continue;
                            }

                            if (!entriesToFilter.isEmpty() && !entriesToFilter.contains(value)) {
                                continue;
                            }

                            if (!isNullOrEmpty(value.trim())) {
                                Property<AttributedType> associatedProperty = PropertyQueries
                                        .<AttributedType>createQuery(relationshipClass)
                                        .addCriteria(new NamedPropertyCriteria(memberAttribute.getKey())).getSingleResult();

                                String memberBaseDN = value.substring(value.indexOf(",") + 1);
                                String dn = value.substring(0, value.indexOf(","));

                                NamingEnumeration<SearchResult> result = this.operationManager.search(memberBaseDN, dn, null);

                                if (!result.hasMore()) {
                                    throw new IdentityManagementException("Associated entry does not exists [" + value + "].");
                                }

                                if (property.getJavaClass().isAssignableFrom(ownerRelType.getClass())) {
                                    V relationship = newInstance(relationshipClass);

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
        } catch (Exception e) {
            throw MESSAGES.queryRelationshipFailed(query, e);
        } finally {
            safeClose(search);
        }

        return results;
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

    private void addRelationship(Relationship relationship) {
        LDAPMappingConfiguration relationshipConfig = getMappingConfig(relationship.getClass());
        Property<AttributedType> property = PropertyQueries
                .<AttributedType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(relationshipConfig.getRelatedAttributedType()))
                .getSingleResult();
        AttributedType ownerType = property.getValue(relationship);
        Attributes attributes = this.operationManager.getAttributes(ownerType.getId(), getBaseDN(ownerType), relationshipConfig);

        for (String relationshipTypeProperty : relationshipConfig.getMappedProperties().keySet()) {
            Property<AttributedType> relationshipProperty = PropertyQueries
                    .<AttributedType>createQuery(relationship.getClass())
                    .addCriteria(new NamedPropertyCriteria(relationshipTypeProperty))
                    .getSingleResult();

            Attribute relationshipAttribute = attributes.get(relationshipConfig.getMappedProperties().get(relationshipTypeProperty));

            if (relationshipAttribute != null) {
                String memberDN = getBindingDN(relationshipProperty.getValue(relationship));

                List<String> membersToRemove = new ArrayList<String>();

                try {
                    NamingEnumeration all = relationshipAttribute.getAll();

                    while (all.hasMore()) {
                        Object next = all.next();

                        if (next.toString().trim().equals(getEmptyAttributeValue().trim())) {
                            membersToRemove.add(getEmptyAttributeValue());
                            membersToRemove.add(getEmptyAttributeValue().trim());
                        }

                        if (next.toString().toLowerCase().equals(memberDN.toLowerCase())) {
                            membersToRemove.add(next.toString());
                        }
                    }

                    for (String memberToRemove : membersToRemove) {
                        relationshipAttribute.remove(memberToRemove);
                    }
                } catch (NamingException ne) {
                    throw new IdentityManagementException("Could not iterate over members for relationship [" + relationship + "].", ne);
                }

                relationshipAttribute.add(memberDN);

                this.operationManager.modifyAttribute(getBindingDN(ownerType), relationshipAttribute);
            }
        }
    }

    private void removeRelationship(final Relationship attributedType) {
        Relationship relationship = (Relationship) attributedType;
        LDAPMappingConfiguration mappingConfig = getMappingConfig(relationship.getClass());

        Property<AttributedType> ownerTypeProperty = PropertyQueries
                .<AttributedType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(mappingConfig.getRelatedAttributedType()))
                .getSingleResult();
        AttributedType ownerType = ownerTypeProperty.getValue(relationship);
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
                mappedAttribute.add(getEmptyAttributeValue());
            }

            this.operationManager.modifyAttribute(getBindingDN(ownerType), mappedAttribute);
        }
    }

    private AttributedType populateAttributedType(final IdentityContext context, SearchResult searchResult, AttributedType attributedType) {
        try {
            String nameInNamespace = searchResult.getNameInNamespace();
            String entryDN = nameInNamespace.substring(nameInNamespace.indexOf(COMMA) + 1);
            Attributes attributes = searchResult.getAttributes();

            if (attributedType == null) {
                attributedType = newInstance(getConfig().getSupportedTypeByBaseDN(entryDN, getEntryObjectClasses(attributes)));
            }

            LDAPMappingConfiguration mappingConfig = getMappingConfig(attributedType.getClass());
            NamingEnumeration<? extends Attribute> ldapAttributes = attributes.getAll();

            while (ldapAttributes.hasMore()) {
                Attribute ldapAttribute = ldapAttributes.next();
                Object value = ldapAttribute.get();

                if (ldapAttribute.getID().toLowerCase().equals(getConfig().getUniqueIdentifierAttributeName().toLowerCase())) {
                    attributedType.setId(this.operationManager.decodeEntryUUID(value));
                } else {
                    List<Property<Object>> properties = PropertyQueries
                            .createQuery(attributedType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                    for (Property property : properties) {
                        String ldapAttributeName = mappingConfig.getMappedProperties().get(property.getName());

                        if (ldapAttributeName != null && ldapAttributeName.toLowerCase().equals(ldapAttribute.getID().toLowerCase())) {
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
            }

            LDAPMappingConfiguration entryConfig = getMappingConfig(attributedType.getClass());

            if (mappingConfig.getParentMembershipAttributeName() != null) {
                StringBuilder filter = new StringBuilder("(&");

                filter.append("(").append(getObjectClassesFilter(entryConfig)).append(")").append("(").append(mappingConfig.getParentMembershipAttributeName()).append(EQUAL).append("").append(getBindingName(attributedType)).append(COMMA).append(entryDN).append(")");

                filter.append(")");

                //TODO: if the type does not define a baseDN, we can try to load from the 'entryDN' first. Usually a child is on the same DN as its parent.

                NamingEnumeration<SearchResult> search = this.operationManager.search(getConfig().getBaseDN(), filter.toString(), entryConfig);

                try {
                    while (search.hasMore()) {
                        SearchResult next = search.next();

                        Property<AttributedType> parentProperty = PropertyQueries
                                .<AttributedType>createQuery(attributedType.getClass())
                                .addCriteria(new TypedPropertyCriteria(attributedType.getClass())).getFirstResult();

                        if (parentProperty != null) {
                            String baseDN = next.getNameInNamespace().substring(next.getNameInNamespace().indexOf(",") + 1);
                            Class<? extends AttributedType> baseDNType = getConfig().getSupportedTypeByBaseDN(baseDN, getEntryObjectClasses(attributes));

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
                        propertyValue = getEmptyAttributeValue();
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
                    entryAttributes.put(MEMBER, getEmptyAttributeValue());
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

    NamingEnumeration<SearchResult> lookupEntryByID(String id, String baseDN, LDAPMappingConfiguration mappingConfiguration) {
        return this.operationManager.lookupById(baseDN, id, mappingConfiguration);
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

        if (baseDN == null) {
            baseDN = getConfig().getBaseDN();
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
     *
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

    private boolean isEmptyMember(final String value) {
        return value.contains(getEmptyAttributeValue());
    }

    private String getEmptyAttributeValue() {
        return " ";
    }

}