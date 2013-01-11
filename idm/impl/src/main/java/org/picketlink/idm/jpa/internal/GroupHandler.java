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

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupHandler extends IdentityTypeHandler<Group>{

    public GroupHandler(JPAIdentityStoreConfiguration config) {
        super(config);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Group fromGroup, JPAIdentityStore store) {
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_NAME, fromGroup.getName(), true);

        if (fromGroup.getParentGroup() != null) {
            Object parentIdentity = store.lookupIdentityObjectById(fromGroup.getParentGroup().getId());

            if (parentIdentity == null) {
                store.add(fromGroup.getParentGroup());
                parentIdentity = store.lookupIdentityObjectById(fromGroup.getParentGroup().getId());
            }

            setModelPropertyValue(toIdentity, PropertyType.GROUP_PARENT, parentIdentity, true);
        }
    }
    
    @Override
    void remove(Object identity, Group identityType, JPAIdentityStore store) {
        disassociateChildren(identityType,store);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Group fromIdentityType, JPAIdentityStore store) {
        return new GroupCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Group fromIdentityType, JPAIdentityStore store) {
        return new GroupUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Group fromIdentityType, JPAIdentityStore store) {
        return new GroupDeletedEvent(fromIdentityType);
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

        Join<?, ?> join = root
                .join(getConfig().getModelProperty(PropertyType.GROUP_PARENT).getName());

        predicates.add(builder.equal(
                join.get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
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
        String name = getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_NAME);

        Object parentInstance = getModelPropertyValue(Object.class, identity, PropertyType.GROUP_PARENT);

        SimpleGroup group = null;

        if (parentInstance != null) {
            String parentId = getModelPropertyValue(String.class, parentInstance, PropertyType.IDENTITY_NAME);

            Group parent = store.getGroup(parentId);

            group = new SimpleGroup(name, parent);
        } else {
            group = new SimpleGroup(name);
        }

        return group;
    }
    
    @Override
    public List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria,store);
        CriteriaBuilder builder = criteria.getBuilder();
        
        if (queryParameter.equals(Group.NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(Group.PARENT)) {
            Join<Object, Object> join = criteria.getRoot().join(getConfig().getModelProperty(
                    PropertyType.GROUP_PARENT).getName());

            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(IdentityType.HAS_MEMBER)) {
            
            // TODO rewrite using relationships
            /*
            for (Object object : parameterValues) {
                Property<Object> memberModelProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
                Property<Object> groupModelProperty = store.getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                Subquery<?> subquery = criteria.getCriteria().subquery(store.getConfig().getMembershipClass());
                Root fromProject = subquery.from(store.getConfig().getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(groupModelProperty.getName()));

                Predicate conjunction = criteria.getBuilder().conjunction();

                conjunction.getExpressions().add(
                        criteria.getBuilder().equal(fromProject.get(groupModelProperty.getName()), criteria.getRoot()));
                conjunction.getExpressions().add(
                        criteria.getBuilder().equal(fromProject.get(memberModelProperty.getName()), store.lookupIdentityObjectById((IdentityType) object)));

                subquery.where(conjunction);
                
                predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
            }*/
        }
        
        return predicates;
    }

}
