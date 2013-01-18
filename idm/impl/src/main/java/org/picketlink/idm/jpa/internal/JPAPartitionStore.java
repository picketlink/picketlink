/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.jpa.internal;

import static org.picketlink.idm.jpa.internal.JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * @author Pedro Silva
 *
 */
public class JPAPartitionStore implements PartitionStore {

    private JPAIdentityStore identityStore;

    public JPAPartitionStore(JPAIdentityStore identityStore) {
        this.identityStore = identityStore;
        
        if (getRealm(Realm.DEFAULT_REALM) == null) {
            createDefaultRealm();
        }
    }
    
    @Override
    public void createPartition(Partition partition) {
        Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
        Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        Class<?> partitionClass = getConfig().getPartitionClass();
        Object partitionObject = null;

        try {
            partitionObject = partitionClass.newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Could not instantiate Partition class [" + partitionClass.getName() + "]");
        }

        String id = getContext().getIdGenerator().generate();

        partition.setId(id);

        idProperty.setValue(partitionObject, partition.getId());
        nameProperty.setValue(partitionObject, partition.getName());
        typeProperty.setValue(partitionObject, partition.getClass().getName());

        if (Tier.class.isInstance(partition)) {
            Tier tier = (Tier) partition;
            Tier parentTier = tier.getParent();

            if (parentTier != null) {
                Property<Object> parentProperty = getConfig().getModelProperty(PropertyType.PARTITION_PARENT);
                parentProperty.setValue(partitionObject, lookupPartitionObject(parentTier));
            }
        }

        EntityManager em = getEntityManager();

        em.persist(partitionObject);
        em.flush();
    }

    private IdentityStoreInvocationContext getContext() {
        return this.identityStore.getContext();
    }
    
    private JPAIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }
    
    @Override
    public Realm getRealm(String realmName) {
        return convertPartitionEntityToRealm(lookupPartitionEntityByName(Realm.class, realmName));
    }
    
    @Override
    public Tier getTier(String tierName) {
        return convertPartitionEntityToTier(lookupPartitionEntityByName(Tier.class, tierName));
    }
    
    @Override
    public void removePartition(Partition partition) {
        if (partition.getId() == null) {
            throw new IdentityManagementException("No identifier provided.");
        }

        Object partitionObject = lookupPartitionObject(partition);

        if (partitionObject == null) {
            throw new IdentityManagementException("No Partition found with the given id [" + partition.getId() + "].");
        }

        EntityManager entityManager = getEntityManager();

        List<?> associatedIdentityTypes = getIdentityTypesForPartition(partitionObject);

        if (!associatedIdentityTypes.isEmpty()) {
            throw new IdentityManagementException(
                    "Partition could not be removed. There are IdentityTypes associated with it. Remove them first.");
        }

        List<?> childPartitions = getChildPartitions(partitionObject);

        if (!childPartitions.isEmpty()) {
            throw new IdentityManagementException(
                    "Partition could not be removed. There are child partitions associated with it. Remove them first.");
        }

        entityManager.remove(partitionObject);
        entityManager.flush();
    }
    
    protected Object lookupPartitionEntityByName(Class<? extends Partition> partitionType, String name) {
        if (name == null) {
            throw new IdentityManagementException("Tier name was not provided.");
        }

        EntityManager entityManager = getEntityManager();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        Class<?> partitionClass = getConfig().getPartitionClass();

        CriteriaQuery<?> criteria = builder.createQuery(partitionClass);
        Root<?> root = criteria.from(partitionClass);

        Predicate whereType = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.PARTITION_TYPE).getName()), partitionType.getName());
        Predicate whereName = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.PARTITION_NAME).getName()), name);

        criteria.where(whereName, whereType);

        Object partitionObject = null;

        try {
            partitionObject = entityManager.createQuery(criteria).getSingleResult();
        } catch (NonUniqueResultException nuoe) {
            throw new IdentityManagementException("Abiguous Tier found with the given name [" + name + "]");
        } catch (NoResultException ignore) {
        }

        return partitionObject;
    }
    
    private void createDefaultRealm() {
        createPartition(new Realm(Realm.DEFAULT_REALM));
    }
    
    protected Partition convertPartitionEntityToPartition(Object partitionObject) {
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        String type = typeProperty.getValue(partitionObject).toString();

        Partition partition = null;

        if (Realm.class.getName().equals(type)) {
            partition = convertPartitionEntityToRealm(partitionObject);
        } else if (Tier.class.getName().equals(type)) {
            partition = convertPartitionEntityToTier(partitionObject);
        } else {
            throw new IdentityManagementException("Unsupported Partition type [" + type + "].");
        }

        return partition;
    }

    protected Object lookupPartitionObject(Partition partition) {
        EntityManager entityManager = getEntityManager();
        return entityManager.find(getConfig().getPartitionClass(), partition.getId());
    }
    
    private List<?> getChildPartitions(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getPartitionClass());
        Root<?> root = criteria.from(getConfig().getPartitionClass());

        Predicate wherePartition = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.PARTITION_PARENT).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }
 
    private List<?> getIdentityTypesForPartition(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());

        Predicate wherePartition = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }
 
    private Realm convertPartitionEntityToRealm(Object partitionObject) {
        Realm realm = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Realm.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);

                realm = new Realm(nameProperty.getValue(partitionObject).toString());

                realm.setId(idProperty.getValue(partitionObject).toString());
            }
        }

        return realm;
    }
    
    private Tier convertPartitionEntityToTier(Object partitionObject) {
        Tier tier = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Tier.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);
                Property<Object> parentProperty = getConfig().getModelProperty(PropertyType.PARTITION_PARENT);

                Object parentTierObject = parentProperty.getValue(partitionObject);

                if (parentTierObject != null) {
                    tier = new Tier(nameProperty.getValue(partitionObject).toString(),
                            convertPartitionEntityToTier(parentTierObject));
                } else {
                    tier = new Tier(nameProperty.getValue(partitionObject).toString());
                }

                tier.setId(idProperty.getValue(partitionObject).toString());
            }
        }

        return tier;
    }
    
    private EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw new IllegalStateException("Error while trying to determine EntityManager - context parameter not set.");
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }
}
