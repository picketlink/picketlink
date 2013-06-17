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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld;
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld.PropertyType;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.AttributedType.AttributeParameter;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.SecurityContext;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>
 * Base class that provides some common functionality for {@link IdentityType} types.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class IdentityTypeHandler<T extends IdentityType> {

    // Map queryParameters to actual names of properties in JPA tables
    private Map<QueryParameter, PropertyType> sortParametersMapping = new HashMap<QueryParameter, PropertyType>();

    public IdentityTypeHandler() {
        sortParametersMapping.put(IdentityType.ID, PropertyType.IDENTITY_ID);
        sortParametersMapping.put(IdentityType.PARTITION, PropertyType.IDENTITY_PARTITION);
        sortParametersMapping.put(IdentityType.ENABLED, PropertyType.IDENTITY_ENABLED);
        sortParametersMapping.put(IdentityType.CREATED_DATE, PropertyType.IDENTITY_CREATION_DATE);
        sortParametersMapping.put(IdentityType.EXPIRY_DATE, PropertyType.IDENTITY_EXPIRY_DATE);
    }

    /**
     * <p>
     * Creates a {@link IdentityType} instance using the information from the given Identity Class instance. This method already
     * provides the mapping for the common properties for all {@link IdentityType} types.
     * </p>
     *
     * @param identity
     * @return
     */
    public T createIdentityType(SecurityContext context, Object identity, JPAIdentityStore store) {
        T identityType = doCreateIdentityType(context, identity, store);

        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        identityType.setId(jpaConfig.getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_ID));
        identityType.setEnabled(jpaConfig.getModelPropertyValue(Boolean.class, identity, PropertyType.IDENTITY_ENABLED));

        Object partitionObject = jpaConfig.getModelPropertyValue(jpaConfig.getPartitionClass(), identity,
                PropertyType.IDENTITY_PARTITION);

        Partition partition = store.convertPartitionEntityToPartition(partitionObject);

        identityType.setPartition(partition);

        identityType
                .setExpirationDate(jpaConfig.getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_EXPIRY_DATE));
        identityType.setCreatedDate(jpaConfig.getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_CREATION_DATE));

        return identityType;
    }

    /**
     * <p>
     * Creates a Identity Class instance using the information from the given {@link IdentityType}.
     * </p>
     *
     * @param fromIdentityType
     * @return
     */
    public Object createEntity(SecurityContext context, T fromIdentityType, JPAIdentityStore store) {
        Object identity = null;

        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        try {
            identity = jpaConfig.getIdentityClass().newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(jpaConfig.getIdentityClass().getName(), e);
        }

        String newGeneratedId = context.getIdGenerator().generate();

        jpaConfig.setModelPropertyValue(identity, PropertyType.IDENTITY_ID, newGeneratedId, true);

        fromIdentityType.setId(newGeneratedId);

        populateEntity(context, identity, fromIdentityType, store);

        return identity;
    }

    /**
     * <p>
     * Populates the given {@link Object} argument representing a Identity Class (from the config) with the information from the
     * specified {@link IdentityType}.
     * </p>
     *
     * @param toIdentity
     * @param fromIdentityType
     */
    protected void populateEntity(SecurityContext context, Object toIdentity, T fromIdentityType, JPAIdentityStore store) {
        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        // populate the common properties from IdentityType
        String identityDiscriminator = jpaConfig.getIdentityTypeDiscriminator(fromIdentityType.getClass());

        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_DISCRIMINATOR, identityDiscriminator, true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ENABLED, fromIdentityType.isEnabled(), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_CREATION_DATE, fromIdentityType.getCreatedDate(),
                true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_EXPIRY_DATE, fromIdentityType.getExpirationDate());

        doPopulateIdentityInstance(context, toIdentity, fromIdentityType, store);
    }

    /**
     * <p>
     * Logic to be executed before removing the given {@link IdentityType}. The <code>identity</code> argument refers to a
     * specific Identity Class that maps to the given {@link IdentityType} instance.
     * </p>
     *
     * @param identity
     * @param identityType
     */
    void remove(SecurityContext context, Object identity, T identityType, JPAIdentityStore store) {

    }

    /**
     * <p>
     * Returns a {@link List} of {@link Predicate} to be used during the query execution. This method already provides the
     * mapping for the common properties for all {@link IdentityType} types.
     * </p>
     *
     * @param criteria
     * @return
     */
    public List<Predicate> getPredicate(SecurityContext context, JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        findById(criteria, predicates, store);
        findByPartition(context, criteria, predicates, store);
        findByEnabled(criteria, predicates, store);
        findByCreationDate(criteria, predicates, store);
        findByExpiryDate(criteria, predicates, store);
        findByCreatedAfter(criteria, predicates, store);
        findByExpiryAfter(criteria, predicates, store);
        findByCreatedBefore(criteria, predicates, store);
        findByExpiryBefore(criteria, predicates, store);
        findByGroupRole(context, criteria, predicates, store);
        findByMemberOf(context, criteria, predicates, store);
        findByHasRole(context, criteria, predicates, store);
        findByAttributes(criteria, predicates, store);

        return predicates;
    }

    /**
     * Return list of {@link Order} instances to be used for sorting during the query execution.
     *
     * @param criteria criteria which encapsulate all the parameters and JPA builder
     * @return list of orders to be used during identity query execution
     */
    public List<Order> getOrders(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Order> orders = new ArrayList<Order>();

        QueryParameter[] orderParameters = criteria.getIdentityQuery().getSortParameters();

        // Use default sorting parameters for each identity Typestore
        if (orderParameters == null || orderParameters.length == 0) {
            orderParameters = IDMUtil.getDefaultParamsForSorting(criteria.getIdentityQuery().getIdentityType());
        }

        for (QueryParameter queryParam : orderParameters) {
            PropertyType propertyType = getSortParametersMapping().get(queryParam);

            if (propertyType != null) {
                String propertyName = store.getConfig().getModelProperty(propertyType).getName();

                Order orderToAdd;
                if (criteria.getIdentityQuery().isSortAscending()) {
                    orderToAdd = criteria.getBuilder().asc(criteria.getRoot().get(propertyName));
                } else {
                    orderToAdd = criteria.getBuilder().desc(criteria.getRoot().get(propertyName));
                }

                orders.add(orderToAdd);
            } else {
                throw MESSAGES.notSortableQueryParameter(queryParam);
            }
        }

        return orders;
    }

    /**
     * <p>
     * Subclasses should override this method to create a specific {@link IdentityType} given the provided Identity Class
     * instance.
     * </p>
     *
     * @param identity
     * @return
     */
    protected abstract T doCreateIdentityType(SecurityContext context, Object identity, JPAIdentityStore store);

    /**
     * <p>
     * Subclasses should override this method to populate the given Identity Class instance with the specific information for a
     * given {@link IdentityType}.
     * </p>
     *
     * @param toIdentity
     * @param fromIdentityType
     */
    protected abstract void doPopulateIdentityInstance(SecurityContext context, Object toIdentity, T fromIdentityType,
            JPAIdentityStore store);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByAttributes(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Map<QueryParameter, Object[]> parameters = criteria.getIdentityQuery().getParameters(
                IdentityType.AttributeParameter.class);

        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        Set<Entry<QueryParameter, Object[]>> entrySet = parameters.entrySet();

        for (Entry<QueryParameter, Object[]> entry : entrySet) {
            AttributeParameter customParameter = (AttributeParameter) entry.getKey();
            Object[] attributeValues = entry.getValue();

            String[] valuesToSearch = new String[attributeValues.length];

            for (int i = 0; i < attributeValues.length; i++) {
                valuesToSearch[i] = Base64.encodeObject((Serializable) attributeValues[i]);
            }

            Subquery<?> subquery = criteria.getCriteria().subquery(jpaConfig.getAttributeClass());
            Root fromProject = subquery.from(jpaConfig.getAttributeClass());
            subquery.select(fromProject.get(jpaConfig.getModelProperty(PropertyType.ATTRIBUTE_IDENTITY).getName()));

            Predicate conjunction = criteria.getBuilder().conjunction();

            conjunction.getExpressions().add(
                    criteria.getBuilder().equal(
                            fromProject.get(jpaConfig.getModelProperty(PropertyType.ATTRIBUTE_NAME).getName()),
                            customParameter.getName()));
            conjunction.getExpressions().add(
                    (fromProject.get(jpaConfig.getModelProperty(PropertyType.ATTRIBUTE_VALUE).getName())
                            .in((Object[]) valuesToSearch)));

            subquery.where(conjunction);

            subquery.groupBy(subquery.getSelection()).having(
                    criteria.getBuilder().equal(criteria.getBuilder().count(subquery.getSelection()), valuesToSearch.length));

            predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByHasRole(SecurityContext context, JPACriteriaQueryBuilder criteria, List<Predicate> predicates,
            JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.HAS_ROLE);

        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        if (parameterValues != null) {
            for (Object role : parameterValues) {
                if (!Role.class.isInstance(role)) {
                    throw MESSAGES.queryUnsupportedParameterValue("IdentityType.HAS_ROLE", role);
                }

                RelationshipQuery<Grant> query = context.getIdentityManager().createRelationshipQuery(Grant.class);

                query.setParameter(Grant.ROLE, role);

                List<Grant> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (Grant memberships : resultList) {
                        relIds.add(memberships.getAssignee().getId());
                    }

                    predicates.add(criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()).in(relIds));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByMemberOf(SecurityContext context, JPACriteriaQueryBuilder criteria, List<Predicate> predicates,
            JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.MEMBER_OF);

        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        if (parameterValues != null) {
            for (Object group : parameterValues) {
                if (!Group.class.isInstance(group)) {
                    throw MESSAGES.queryUnsupportedParameterValue("IdentityType.MEMBER_OF", group);
                }

                RelationshipQuery<GroupMembership> query = context.getIdentityManager().createRelationshipQuery(
                        GroupMembership.class);

                query.setParameter(GroupMembership.GROUP, group);

                List<GroupMembership> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (GroupMembership memberships : resultList) {
                        relIds.add(memberships.getMember().getId());
                    }

                    predicates.add(criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()).in(relIds));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void findByGroupRole(SecurityContext context, JPACriteriaQueryBuilder criteria, List<Predicate> predicates,
            JPAIdentityStore store) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.HAS_GROUP_ROLE);

        if (parameterValues != null) {
            for (Object object : parameterValues) {
                if (!GroupRole.class.isInstance(object)) {
                    throw MESSAGES.queryUnsupportedParameterValue("IdentityType.HAS_GROUP_ROLE", object);
                }

                GroupRole groupRole = (GroupRole) object;

                RelationshipQuery<GroupRole> query = context.getIdentityManager().createRelationshipQuery(GroupRole.class);

                query.setParameter(GroupRole.ASSIGNEE, groupRole.getAssignee());
                query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                query.setParameter(GroupRole.ROLE, groupRole.getRole());

                List<GroupRole> resultList = query.getResultList();

                JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (GroupRole memberships : resultList) {
                        relIds.add(memberships.getAssignee().getId());
                    }

                    predicates.add(criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()).in(relIds));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    private void findByExpiryBefore(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_BEFORE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(
                            store.getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByCreatedBefore(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_BEFORE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(
                            store.getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByExpiryAfter(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_AFTER);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(
                            store.getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByCreatedAfter(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_AFTER);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(
                            store.getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByExpiryDate(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_DATE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    parameterValues[0]));
        }
    }

    private void findByCreationDate(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_DATE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    parameterValues[0]));
        }
    }

    private void findByEnabled(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.ENABLED);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PropertyType.IDENTITY_ENABLED).getName()),
                    parameterValues[0]));
        }
    }

    private void findByPartition(SecurityContext context, JPACriteriaQueryBuilder criteria, List<Predicate> predicates,
            JPAIdentityStore store) {
        JPAIdentityStoreConfigurationOld config = store.getConfig();

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.PARTITION);

        if (parameterValues != null) {
            Partition partition = (Partition) parameterValues[0];

            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(config.getModelProperty(PropertyType.IDENTITY_PARTITION).getName()),
                    store.lookupAndCreatePartitionObject(context, partition)));
        } else {
            Join<Object, Object> joinPartition = criteria.getRoot().join(
                    config.getModelProperty(PropertyType.IDENTITY_PARTITION).getName());

            if (criteria.getIdentityQuery().getParameter(IdentityType.PARTITION) == null) {
                List<String> partitionIds = store.getAllowedPartitionIds(context, context.getPartition());

                partitionIds.add(context.getPartition().getId());

                predicates.add(criteria.getBuilder()
                        .in(joinPartition.get(config.getModelProperty(PropertyType.PARTITION_ID).getName()))
                        .value(partitionIds));
            }
        }
    }

    private void findById(JPACriteriaQueryBuilder criteria, List<Predicate> predicates, JPAIdentityStore store) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.ID);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()),
                    parameterValues[0]));
        }
    }

    // protected void setModelPropertyValue(Object identity, PropertyType propertyType, Object value, boolean notNull,
    // JPAIdentityStoreConfiguration config) {
    // config.setModelPropertyValue(identity, propertyType, value, notNull);
    // }
    //
    // protected void setModelPropertyValue(Object identity, PropertyType propertyType, Object value,
    // JPAIdentityStoreConfiguration config) {
    // config.setModelPropertyValue(identity, propertyType, value);
    // }

    protected Map<QueryParameter, PropertyType> getSortParametersMapping() {
        return sortParametersMapping;
    }

}
