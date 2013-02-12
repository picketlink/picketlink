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

package org.picketlink.idm.jpa.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.common.util.Base64;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * @author Pedro Silva
 * 
 */
public class JPACredentialStore implements CredentialStore {

    private JPAIdentityStore identityStore;

    public JPACredentialStore(JPAIdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        checkCredentialClassProvided();

        Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

        Object newCredential = null;

        try {
            newCredential = getConfig().getCredentialClass().newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Could not instantiate credential class ["
                    + getConfig().getCredentialClass().getName() + "].", e);
        }

        Date effectiveDate = storage.getEffectiveDate();

        if (effectiveDate == null) {
            effectiveDate = new Date();
        }

        Object agentInstance = this.identityStore.lookupIdentityObjectById(agent.getId());

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        identityTypeProperty.setValue(newCredential, agentInstance);
        typeProperty.setValue(newCredential, storage.getClass().getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        EntityManager em = getEntityManager();

        em.persist(newCredential);

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        Property<Object> attributeName = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME);
        Property<Object> attributeValue = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE);
        Property<Object> attributeCredential = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

        for (Property<Object> property : annotatedTypes) {
            Object newCredentialAttribute = null;

            try {
                newCredentialAttribute = this.getConfig().getCredentialAttributeClass().newInstance();
            } catch (Exception e) {
                throw new IdentityManagementException("Could not instantiate credential attribute class ["
                        + getConfig().getCredentialAttributeClass().getName() + "].", e);
            }

            attributeName.setValue(newCredentialAttribute, property.getName());
            attributeValue.setValue(newCredentialAttribute, Base64.encodeObject((Serializable) property.getValue(storage)));
            attributeCredential.setValue(newCredentialAttribute, newCredential);

            em.persist(newCredentialAttribute);
        }

        em.flush();
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = this.identityStore.lookupIdentityObjectById(agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> result = em.createQuery(criteria).getResultList();

        List<T> storages = new ArrayList<T>();

        for (Object object : result) {
            storages.add(convertToCredentialStorage(object, storageClass));
        }

        return storages;
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();
        return convertToCredentialStorage(retrieveLastCredentialEntity(agent, storageClass), storageClass);
    }

    protected void removeCredentials(Object object) {
        if (getConfig().getCredentialClass() != null) {
            EntityManager em = getEntityManager();
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
            Root<?> root = criteria.from(getConfig().getCredentialClass());
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY).getName()),
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();

            for (Object credential : results) {
                CriteriaQuery<?> attributeCriteria = builder.createQuery(getConfig().getCredentialAttributeClass());
                Root<?> attributeRoot = attributeCriteria.from(getConfig().getCredentialAttributeClass());
                List<Predicate> attributePredicates = new ArrayList<Predicate>();

                Property<Object> attributeCredential = getConfig().getModelProperty(
                        PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

                attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), credential));

                List<?> attributes = em.createQuery(attributeCriteria).getResultList();

                for (Object attribute : attributes) {
                    em.remove(attribute);
                }

                em.remove(credential);
            }
        }
    }

    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this.identityStore);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.validate(credentials, this.identityStore);
    }

    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this.identityStore);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this.identityStore, effectiveDate, expiryDate);
    }

    private IdentityStoreInvocationContext getContext() {
        return this.identityStore.getContext();
    }

    private <T extends CredentialStorage> T convertToCredentialStorage(Object instance, Class<T> storageClass) {
        T storage = null;

        if (instance != null) {
            try {
                storage = storageClass.newInstance();
            } catch (Exception e) {
                throw new IdentityManagementException("Could not instantiate storage class [" + storageClass.getName() + "].",
                        e);
            }

            Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);
            Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

            List<Property<Object>> effectiveDateProperty = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria("effectiveDate")).getResultList();

            effectiveDateProperty.get(0).setValue(storage, effectiveProperty.getValue(instance));

            List<Property<Object>> expiryDateProperty = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria("expiryDate")).getResultList();

            expiryDateProperty.get(0).setValue(storage, expiryProperty.getValue(instance));

            EntityManager em = getEntityManager();

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> attributeCriteria = builder.createQuery(getConfig().getCredentialAttributeClass());
            Root<?> attributeRoot = attributeCriteria.from(getConfig().getCredentialAttributeClass());
            List<Predicate> attributePredicates = new ArrayList<Predicate>();

            Property<Object> attributeCredential = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

            attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), instance));

            attributeCriteria.where(attributePredicates.toArray(new Predicate[attributePredicates.size()]));

            List<?> attributes = em.createQuery(attributeCriteria).getResultList();

            Property<Object> attributeName = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME);
            Property<Object> attributeValue = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE);

            for (Object attribute : attributes) {
                String name = attributeName.getValue(attribute).toString();
                String value = attributeValue.getValue(attribute).toString();

                List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                        .addCriteria(new NamedPropertyCriteria(name)).getResultList();

                if (annotatedTypes.isEmpty()) {
                    throw new IdentityManagementException("Could not find property [" + attributeName.getName()
                            + "] on CredentialStorage [" + storageClass.getName() + "].");
                } else if (annotatedTypes.size() > 1) {
                    throw new IdentityManagementException("Ambiguos property [" + attributeName.getName()
                            + "] on CredentialStorage [" + storageClass.getName() + "].");
                }

                Property<Object> property = annotatedTypes.get(0);

                property.setValue(storage, Base64.decodeToObject(value));
            }
        }

        return storage;
    }

    /**
     * <p>
     * Returns the last stored credential for the given {@link Agent} considering the given storageClass. The last credential is
     * the one which the effectiveDate is more close to the current date.
     * </p>
     * 
     * @param agent
     * @param storageClass
     * @return
     */
    private <T> Object retrieveLastCredentialEntity(Agent agent, Class<T> storageClass) {
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = this.identityStore.lookupIdentityObjectById(agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date> get(effectiveProperty.getName()), new Date()));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        Object lastCredential = null;

        try {
            List<?> result = em.createQuery(criteria).getResultList();

            if (!result.isEmpty()) {
                lastCredential = result.get(0);
            }
        } catch (NoResultException ignore) {
        } catch (Exception e) {
            throw new IdentityManagementException("Could not query credentials.", e);
        }

        return lastCredential;
    }

    private JPAIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }

    private void checkCredentialClassProvided() {
        if (getConfig().getCredentialClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store credentials.");
        }
    }

    private EntityManager getEntityManager() {
        return this.identityStore.getEntityManager();
    }
}
