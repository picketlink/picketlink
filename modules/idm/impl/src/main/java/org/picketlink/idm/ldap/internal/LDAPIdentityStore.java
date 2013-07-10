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
import java.util.List;
import java.util.TimeZone;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
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
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import static java.util.Map.Entry;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.COMMA;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE;
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
        BasicAttributes ldapEntryAttributes = extractLDAPAttributes(attributedType);

        BasicAttribute objectClassAttribute = new BasicAttribute(OBJECT_CLASS);

        LDAPMappingConfiguration ldapEntryConfig = getConfig().getMappingConfig(attributedType.getClass());

        for (String objectClassValue : ldapEntryConfig.getObjectClasses()) {
            objectClassAttribute.add(objectClassValue);
        }

        ldapEntryAttributes.put(objectClassAttribute);

        String baseDN = ldapEntryConfig.getBaseDN();

        this.operationManager.createSubContext(getBindingName(attributedType) + COMMA + baseDN, ldapEntryAttributes);
        createCustomAttributes(attributedType);

        NamingEnumeration<SearchResult> search = null;

        try {
            search = this.operationManager.search(baseDN, "(" + getBindingName(attributedType) + ")");

            populateAttributedType(search.next(), attributedType);
        } catch (NamingException ne) {

        }
    }

    @Override
    public void update(IdentityContext context, AttributedType attributedType) {
        BasicAttributes updatedAttributes = extractLDAPAttributes(attributedType);

        NamingEnumeration<javax.naming.directory.Attribute> attributes = updatedAttributes.getAll();

        try {
            while (attributes.hasMore()) {
                javax.naming.directory.Attribute attribute = attributes.next();

                String dn = getBindingName(attributedType) + COMMA + getConfig().getMappingConfig(attributedType.getClass()).getBaseDN();

                this.operationManager.modifyAttribute(dn, attribute);
            }

            createCustomAttributes(attributedType);
        } catch (NamingException ne) {
            throw new IdentityManagementException(ne);
        }
    }

    @Override
    public void remove(IdentityContext context, AttributedType attributedType) {
        this.operationManager.removeEntryById(getConfig().getMappingConfig(attributedType.getClass()).getBaseDN(), attributedType.getId());
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        StringBuffer filter = new StringBuffer("");
        LDAPMappingConfiguration ldapEntryConfig = getConfig().getMappingConfig(identityQuery.getIdentityType());
        List<V> results = new ArrayList<V>();

        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            Object[] queryParameterValues = entry.getValue();

            if (IdentityType.ID.equals(entry.getKey())) {
                NamingEnumeration<SearchResult> resultNamingEnumeration = null;

                resultNamingEnumeration = this.operationManager.lookupById(getConfig().getBaseDN(), queryParameterValues[0].toString());

                try {
                    while (resultNamingEnumeration.hasMore()) {
                        V attributedType = (V) populateAttributedType(resultNamingEnumeration.next(), null);

                        results.add(attributedType);
                    }
                } catch (NamingException ne) {
                    throw new IdentityManagementException(ne);
                }

                return results;
            } else {
                if (AttributeParameter.class.isInstance(entry.getKey())) {
                    AttributeParameter attributeParameter = (AttributeParameter) entry.getKey();

                    String attributeParameterName = attributeParameter.getName();

                    if (attributeParameterName != null) {
                        filter.append("(").append(ldapEntryConfig.getMappedProperties().get(attributeParameterName)).append(LDAPConstants.EQUAL).append(queryParameterValues[0]).append(")");
                    }
                }
            }
        }

        NamingEnumeration<SearchResult> answer = null;

        try {
            String baseDN = getConfig().getBaseDN();

            if (ldapEntryConfig != null) {
                baseDN = ldapEntryConfig.getBaseDN();
            }

            answer = this.operationManager.search(baseDN, filter.toString());

            while (answer.hasMore()) {
                SearchResult searchResult = answer.next();
                results.add((V) populateAttributedType(searchResult, null));
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

    private AttributedType populateAttributedType(SearchResult searchResult, AttributedType attributedType) {
        try {
            String nameInNamespace = searchResult.getNameInNamespace();
            String entryDN = nameInNamespace.substring(nameInNamespace.indexOf(COMMA) + 1);
            Attributes attributes = searchResult.getAttributes();

            if (attributedType == null) {
                attributedType = getConfig().getSupportedTypeByBaseDN(entryDN).newInstance();
            }

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
                    String ldapAttributeName = getConfig().getMappingConfig(attributedType.getClass()).getMappedProperties().get(property.getName());

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

            populateCustomAttributes(attributedType);
        } catch (NamingException e) {
            throw new IdentityManagementException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attributedType;
    }

    private String getBindingName(AttributedType attributedType) {
        LDAPMappingConfiguration ldapEntryConfig = getConfig().getMappingConfig(attributedType.getClass());

        for (Property<String> property : PropertyQueries
                .<String>createQuery(attributedType.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList()) {
            AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);

            String ldapAttributeName = ldapEntryConfig.getMappedProperties().get(property.getName());

            if (!isNullOrEmpty(ldapAttributeName)) {
                if (ldapAttributeName.equalsIgnoreCase(ldapEntryConfig.getIdAttributeName())) {
                    String propertyValue = property.getValue(attributedType);

                    if (propertyValue == null) {
                        throw new IdentityManagementException("Null value for id property [" + ldapAttributeName + "].");
                    }

                    return ldapEntryConfig.getIdAttributeName() + LDAPConstants.EQUAL + propertyValue;
                }
            }
        }

        throw new IdentityManagementException("Could not resolve binding name for type [" + attributedType + "].");
    }

    private BasicAttributes extractLDAPAttributes(AttributedType attributedType) {
        BasicAttributes ldapEntryAttributes = new BasicAttributes();

        for (Property<String> property : PropertyQueries
                .<String>createQuery(attributedType.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList()) {
            AttributeProperty attributeProperty = property.getAnnotatedElement().getAnnotation(AttributeProperty.class);
            String propertyValue = property.getValue(attributedType);

            String ldapAttributeName = getConfig().getMappingConfig(attributedType.getClass()).getMappedProperties().get(property.getName());

            if (!isNullOrEmpty(ldapAttributeName)) {
                if (propertyValue == null) {
                    propertyValue = " ";
                }

                ldapEntryAttributes.put(ldapAttributeName, propertyValue);
            }
        }

        return ldapEntryAttributes;
    }

    private void createCustomAttributes(AttributedType attributedType) {
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

    private String getCustomAttributesDN(AttributedType attributedType) {
        return CN + "=custom-attributes" + COMMA + getBindingName(attributedType) + COMMA + getConfig().getMappingConfig(attributedType.getClass()).getBaseDN();
    }
}