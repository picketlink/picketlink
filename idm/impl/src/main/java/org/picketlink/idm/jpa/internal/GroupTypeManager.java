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

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupTypeManager extends IdentityTypeManager<Group>{

    public GroupTypeManager(JPAIdentityStore store) {
        super(store);
    }

    @Override
    protected void fromIdentityType(Object toIdentity, Group fromGroup) {
        getStore().setModelProperty(toIdentity, PROPERTY_IDENTITY_NAME, fromGroup.getName(), true);

        if (fromGroup.getParentGroup() != null) {
            Object parentIdentity = getStore().lookupIdentityObjectById(fromGroup.getParentGroup());

            if (parentIdentity == null) {
                getStore().add(fromGroup.getParentGroup());
                parentIdentity = getStore().lookupIdentityObjectById(fromGroup.getParentGroup());
            }

            getStore().setModelProperty(toIdentity, PROPERTY_PARENT_GROUP, parentIdentity, true);
        }
    }
    
    @Override
    void remove(Object identity, Group identityType) {
        disassociateChilds(identityType);
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
    
    /**
     * <p>
     * Disassociates the given {@link Group} from its childs.
     * </p>
     * 
     * @param group
     */
    private void disassociateChilds(Group group) {
        EntityManager em = getStore().getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root
                .join(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP).getName());

        predicates.add(builder.equal(
                join.get(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME).getName()),
                group.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> resultList = em.createQuery(criteria).getResultList();

        for (Object object : resultList) {
            getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP).setValue(object, null);
            em.merge(object);
        }
    }

    @Override
    protected Group createIdentityType(Object identity) {
        String name = getStore().getModelProperty(String.class, identity, PROPERTY_IDENTITY_NAME);

        Object parentInstance = getStore().getModelProperty(Object.class, identity, PROPERTY_PARENT_GROUP);

        SimpleGroup group = null;

        if (parentInstance != null) {
            String parentId = getStore().getModelProperty(String.class, parentInstance, PROPERTY_IDENTITY_NAME);

            Group parent = getStore().getGroup(parentId);

            group = new SimpleGroup(name, parent);
        } else {
            group = new SimpleGroup(name);
        }

        return group;
    }
    
    @Override
    protected List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria) {
        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria);
        CriteriaBuilder builder = criteria.getBuilder();
        
        if (queryParameter.equals(Group.NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(Group.PARENT)) {
            Join<Object, Object> join = criteria.getRoot().join(getConfig().getModelProperty(PROPERTY_PARENT_GROUP).getName());

            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(IdentityType.HAS_MEMBER)) {
            for (Object object : parameterValues) {
                Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getMembershipClass());
                Root fromProject = subquery.from(getConfig().getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(groupModelProperty.getName()));

                Predicate conjunction = criteria.getBuilder().conjunction();

                conjunction.getExpressions().add(
                        criteria.getBuilder().equal(fromProject.get(groupModelProperty.getName()), criteria.getRoot()));
                conjunction.getExpressions().add(
                        criteria.getBuilder().equal(fromProject.get(memberModelProperty.getName()), getStore().lookupIdentityObjectById((IdentityType) object)));

                subquery.where(conjunction);
                
                predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
            }
        }
        
        return predicates;
    }

}
