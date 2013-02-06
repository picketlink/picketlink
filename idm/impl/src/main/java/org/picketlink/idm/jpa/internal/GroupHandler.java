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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class GroupHandler extends IdentityTypeHandler<Group> {

    public GroupHandler(JPAIdentityStoreConfiguration config) {
        super(config);

        getSortParametersMapping().put(Group.NAME, PropertyType.IDENTITY_NAME);
        // TODO: Parameter for parent group should be likely added as well...
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Group fromGroup, JPAIdentityStore store) {
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION,
                store.lookupPartitionObject(store.getCurrentPartition()), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_NAME, fromGroup.getName(), true);

        if (fromGroup.getParentGroup() != null) {
            Object parentIdentity = store.lookupIdentityObjectById(fromGroup.getParentGroup().getId());

            if (parentIdentity == null) {
                store.add(fromGroup.getParentGroup());
                parentIdentity = store.lookupIdentityObjectById(fromGroup.getParentGroup().getId());
            }

            getConfig().setModelPropertyValue(toIdentity, PropertyType.GROUP_PARENT, parentIdentity, true);
        }
    }

    @Override
    void remove(Object identity, Group identityType, JPAIdentityStore store) {
        disassociateChildren(identityType, store);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Group fromIdentityType) {
        return new GroupCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Group fromIdentityType) {
        return new GroupUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Group fromIdentityType) {
        return new GroupDeletedEvent(fromIdentityType);
    }

    @Override
    protected QueryParameter[] getDefaultSortingParameters() {
        return new QueryParameter[] { Group.NAME };
    }

    /**
     * <p>
     * Disassociates the given {@link Group} from its children.
     * </p>
     * 
     * @param group
     */
    private void disassociateChildren(Group group, JPAIdentityStore store) {
        EntityManager em = store.getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(store.getConfig().getIdentityClass());
        Root<?> root = criteria.from(store.getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(getConfig().getModelProperty(PropertyType.GROUP_PARENT).getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                group.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> resultList = em.createQuery(criteria).getResultList();

        for (Object object : resultList) {
            getConfig().getModelProperty(PropertyType.GROUP_PARENT).setValue(object, null);
            em.merge(object);
        }
    }

    @Override
    protected Group doCreateIdentityType(Object identity, JPAIdentityStore store) {
        SimpleGroup group = null;
        
        Object parentInstance = getConfig().getModelPropertyValue(Object.class, identity, PropertyType.GROUP_PARENT);

        String name = getConfig().getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_NAME);
        
        if (parentInstance != null) {
            String parentId = getConfig().getModelPropertyValue(String.class, parentInstance, PropertyType.IDENTITY_NAME);

            group = new SimpleGroup(name, store.getGroup(parentId));
        } else {
            group = new SimpleGroup(name);
        }

        return group;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(Group.NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(Group.PARENT);

        if (parameterValues != null) {
            Join<Object, Object> join = criteria.getRoot().join(
                    getConfig().getModelProperty(PropertyType.GROUP_PARENT).getName());

            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(Group.HAS_MEMBER);

        if (parameterValues != null) {
            for (Object object : parameterValues) {
                DefaultRelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(GroupMembership.class, store);

                query.setParameter(GroupMembership.MEMBER, object);

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
                            GroupMembership.GROUP.getName()));
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

        return predicates;
    }

    @Override
    public void validate(Group group, JPAIdentityStore store) {
        if (group.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }
        
        if (store.getGroup(group.getName()) != null) {
            throw new IdentityManagementException("Group already exists with the given loginName [" + group.getName() + "] for the given Partition [" + store.getCurrentPartition().getName() + "]");
        }
    }
}
