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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.AttributedType.AttributeParameter;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;

/**
 * <p>
 * Base class that provides some common functionality for {@link IdentityType} types.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class IdentityTypeHandler<T extends IdentityType> {

    private JPAIdentityStoreConfiguration config;

    public IdentityTypeHandler(JPAIdentityStoreConfiguration config) {
        this.config = config;
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
    public T createIdentityType(Object identity, JPAIdentityStore store) {
        T identityType = doCreateIdentityType(identity, store);

        identityType.setId(getConfig().getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_ID));
        identityType.setEnabled(getConfig().getModelPropertyValue(Boolean.class, identity, PropertyType.IDENTITY_ENABLED));

        Object partitionObject = getConfig().getModelPropertyValue(getConfig().getPartitionClass(), identity,
                PropertyType.IDENTITY_PARTITION);

        Partition partition = store.convertPartitionEntityToPartition(partitionObject);

        identityType.setPartition(partition);

        identityType.setExpirationDate(getConfig().getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_EXPIRY_DATE));
        identityType.setCreatedDate(getConfig().getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_CREATION_DATE));

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
    public Object createEntity(T fromIdentityType, JPAIdentityStore store) {
        Object identity = null;

        try {
            identity = getConfig().getIdentityClass().newInstance();
            String id = store.getContext().getIdGenerator().generate();
            getConfig().getModelProperty(PropertyType.IDENTITY_ID).setValue(identity, id);
            fromIdentityType.setId(id);
            populateIdentityInstance(identity, fromIdentityType, store);
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating/populating Identity instance from IdentityType.", e);
        }

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
    protected void populateIdentityInstance(Object toIdentity, T fromIdentityType, JPAIdentityStore store) {
        // populate the common properties from IdentityType
        String identityDiscriminator = getConfig().getIdentityDiscriminator(fromIdentityType.getClass());

        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_DISCRIMINATOR, identityDiscriminator, true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ENABLED, fromIdentityType.isEnabled(), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_CREATION_DATE, fromIdentityType.getCreatedDate(), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_EXPIRY_DATE, fromIdentityType.getExpirationDate());

        doPopulateIdentityInstance(toIdentity, fromIdentityType, store);
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
    void remove(Object identity, T identityType, JPAIdentityStore store) {

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
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        findById(criteria, predicates);
        findByPartition(criteria, store, predicates);
        findByEnabled(criteria, predicates);
        findByCreationDate(criteria, predicates);
        findByExpiryDate(criteria, predicates);
        findByCreatedAfter(criteria, predicates);
        findByExpiryAfter(criteria, predicates);
        findByCreatedBefore(criteria, predicates);
        findByExpiryBefore(criteria, predicates);
        findByGroupRole(criteria, store, predicates);
        findByMemberOf(criteria, store, predicates);
        findByHasRole(criteria, store, predicates);
        findByAttributes(criteria, predicates);

        return predicates;
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
    protected abstract T doCreateIdentityType(Object identity, JPAIdentityStore store);

    /**
     * <p>
     * Subclasses should override this method to populate the given Identity Class instance with the specific information for a
     * given {@link IdentityType}.
     * </p>
     * 
     * @param toIdentity
     * @param fromIdentityType
     */
    protected abstract void doPopulateIdentityInstance(Object toIdentity, T fromIdentityType, JPAIdentityStore store);

    protected abstract AbstractBaseEvent raiseCreatedEvent(T fromIdentityType);

    protected abstract AbstractBaseEvent raiseUpdatedEvent(T fromIdentityType);

    protected abstract AbstractBaseEvent raiseDeletedEvent(T fromIdentityType);

    public void validate(T identityType, JPAIdentityStore store) {

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByAttributes(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Map<QueryParameter, Object[]> parameters = criteria.getIdentityQuery().getParameters(IdentityType.AttributeParameter.class);
        
        Set<Entry<QueryParameter, Object[]>> entrySet = parameters.entrySet();
        
        for (Entry<QueryParameter, Object[]> entry : entrySet) {
            AttributeParameter customParameter = (AttributeParameter) entry.getKey();
            Object[] attributeValues = entry.getValue(); 
                    
            Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getAttributeClass());
            Root fromProject = subquery.from(getConfig().getAttributeClass());
            subquery.select(fromProject.get(getConfig().getModelProperty(
                    PropertyType.RELATIONSHIP_IDENTITY).getName()));

            Predicate conjunction = criteria.getBuilder().conjunction();

            conjunction.getExpressions().add(
                    criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getName()),
                            customParameter.getName()));
            conjunction.getExpressions().add(
                    (fromProject.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE).getName())
                            .in((Object[]) attributeValues)));

            subquery.where(conjunction);

            subquery.groupBy(subquery.getSelection()).having(
                    criteria.getBuilder().equal(criteria.getBuilder().count(subquery.getSelection()), attributeValues.length));

            predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByHasRole(JPACriteriaQueryBuilder criteria, JPAIdentityStore store, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.HAS_ROLE);

        if (parameterValues != null) {
            for (Object roleName : parameterValues) {
                DefaultRelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(Grant.class, store);

                query.setParameter(Grant.ROLE, store.getRole(roleName.toString()));

                List<Grant> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (Grant memberships : resultList) {
                        relIds.add(memberships.getId());
                    }

                    Subquery<?> subquery = criteria.getCriteria().subquery(store.getConfig().getRelationshipIdentityClass());
                    Root fromProject = subquery.from(store.getConfig().getRelationshipIdentityClass());
                    subquery.select(fromProject.get(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY).getName()));
                    Join<Object, Object> join = fromProject.join(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName());

                    List<Predicate> subqueryPredicates = new ArrayList<Predicate>();

                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).getName()),
                            Grant.ASSIGNEE.getName()));
                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                            criteria.getRoot()));
                    subqueryPredicates.add(criteria.getBuilder()
                            .in(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName())).value(relIds));

                    subquery.where(subqueryPredicates.toArray(new Predicate[subqueryPredicates.size()]));

                    predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void findByMemberOf(JPACriteriaQueryBuilder criteria, JPAIdentityStore store, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.MEMBER_OF);

        if (parameterValues != null) {
            for (Object groupName : parameterValues) {
                DefaultRelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(GroupMembership.class, store);

                query.setParameter(GroupMembership.GROUP, store.getGroup(groupName.toString()));

                List<GroupMembership> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (GroupMembership memberships : resultList) {
                        relIds.add(memberships.getId());
                    }

                    Subquery<?> subquery = criteria.getCriteria().subquery(store.getConfig().getRelationshipIdentityClass());
                    Root fromProject = subquery.from(store.getConfig().getRelationshipIdentityClass());
                    subquery.select(fromProject.get(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY).getName()));
                    Join<Object, Object> join = fromProject.join(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName());

                    List<Predicate> subqueryPredicates = new ArrayList<Predicate>();

                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).getName()),
                            GroupMembership.MEMBER.getName()));
                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                            criteria.getRoot()));
                    subqueryPredicates.add(criteria.getBuilder()
                            .in(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName())).value(relIds));

                    subquery.where(subqueryPredicates.toArray(new Predicate[subqueryPredicates.size()]));

                    predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void findByGroupRole(JPACriteriaQueryBuilder criteria, JPAIdentityStore store, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.HAS_GROUP_ROLE);

        if (parameterValues != null) {
            for (Object object : parameterValues) {
                GroupRole groupRole = (GroupRole) object;

                DefaultRelationshipQuery<GroupRole> query = new DefaultRelationshipQuery<GroupRole>(GroupRole.class, store);

                query.setParameter(GroupRole.MEMBER, groupRole.getMember());
                query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                query.setParameter(GroupRole.ROLE, groupRole.getRole());

                List<GroupRole> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (GroupRole memberships : resultList) {
                        relIds.add(memberships.getId());
                    }

                    Subquery<?> subquery = criteria.getCriteria().subquery(store.getConfig().getRelationshipIdentityClass());
                    Root fromProject = subquery.from(store.getConfig().getRelationshipIdentityClass());
                    subquery.select(fromProject.get(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY).getName()));
                    Join<Object, Object> join = fromProject.join(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName());

                    List<Predicate> subqueryPredicates = new ArrayList<Predicate>();

                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).getName()),
                            GroupRole.MEMBER.getName()));
                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                            criteria.getRoot()));
                    subqueryPredicates.add(criteria.getBuilder()
                            .in(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName())).value(relIds));

                    subquery.where(subqueryPredicates.toArray(new Predicate[subqueryPredicates.size()]));

                    predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }
    }

    private void findByExpiryBefore(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_BEFORE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByCreatedBefore(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_BEFORE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByExpiryAfter(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_AFTER);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByCreatedAfter(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_AFTER);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }
    }

    private void findByExpiryDate(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.EXPIRY_DATE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    parameterValues[0]));
        }
    }

    private void findByCreationDate(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.CREATED_DATE);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    parameterValues[0]));
        }
    }

    private void findByEnabled(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.ENABLED);
        
        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ENABLED).getName()),
                    parameterValues[0]));
        }
    }

    private void findByPartition(JPACriteriaQueryBuilder criteria, JPAIdentityStore store, List<Predicate> predicates) {
        Object[] parameterValues;
        parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.PARTITION);

        if (parameterValues != null) {
            Partition partition = (Partition) parameterValues[0];

            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName()),
                    store.lookupPartitionObject(partition)));
        } else {
            Join<Object, Object> joinPartition = criteria.getRoot().join(getConfig().getModelProperty(
                    PropertyType.IDENTITY_PARTITION).getName());
            
            if (criteria.getIdentityQuery().getParameter(IdentityType.PARTITION) == null) {
                List<String> partitionIds = new ArrayList<String>();
                
                partitionIds.add(store.getCurrentRealm().getId());
                
                if (Tier.class.isInstance(store.getCurrentPartition())) {
                    populateAllowedTierIds(partitionIds, (Tier) store.getCurrentPartition());
                }
                
                predicates.add(criteria.getBuilder().in(joinPartition.get(getConfig().getModelProperty(PropertyType.PARTITION_ID).getName())).value(partitionIds));
            }            
        }
    }

    private void findById(JPACriteriaQueryBuilder criteria, List<Predicate> predicates) {
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(IdentityType.ID);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()),
                    parameterValues[0]));
        }
    }
    
    protected JPAIdentityStoreConfiguration getConfig() {
        return this.config;
    }
    
    private void populateAllowedTierIds(List<String> partitionIds, Tier currentPartition) {
        partitionIds.add(currentPartition.getId());
        
        if (currentPartition.getParent() != null) {
            populateAllowedTierIds(partitionIds, currentPartition.getParent());
        }
    }
}
