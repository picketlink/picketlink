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

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_VALUE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ENABLED;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ID;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_EMAIL;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_FIRST_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_LAST_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class JPACriteriaQueryBuilder {

    private JPAIdentityStoreConfiguration config;
    private IdentityQuery<?> identityQuery;
    private EntityManager entityManager;
    private CriteriaBuilder builder;
    private Root<?> root;
    private CriteriaQuery<?> criteria;
    private List<Predicate> predicates = new ArrayList<Predicate>();
    private JPAIdentityStore identityStore;

    public JPACriteriaQueryBuilder(JPAIdentityStore identityStore, IdentityQuery<?> identityQuery) {
        this.identityStore = identityStore;
        this.config = identityStore.getConfig();
        this.identityQuery = identityQuery;
        this.entityManager = identityStore.getEntityManager();
        this.builder = this.entityManager.getCriteriaBuilder();
        
        Class<?> identityClass = this.config.getIdentityClass();
        
        this.criteria = builder.createQuery(identityClass);
        this.root = criteria.from(identityClass);
    }

    public Property<Object> mapToModelProperty(QueryParameter queryParameter) {
        Property<Object> property = null;

        if (queryParameter.equals(Agent.ID)) {
            property = this.config.getModelProperty(PROPERTY_IDENTITY_ID);
        }

        if (queryParameter.equals(User.FIRST_NAME)) {
            property = this.config.getModelProperty(PROPERTY_USER_FIRST_NAME);
        }

        if (queryParameter.equals(User.LAST_NAME)) {
            property = this.config.getModelProperty(PROPERTY_USER_LAST_NAME);
        }

        if (queryParameter.equals(User.EMAIL)) {
            property = this.config.getModelProperty(PROPERTY_USER_EMAIL);
        }

        if (queryParameter.equals(Group.NAME) || queryParameter.equals(Role.NAME)) {
            property = this.config.getModelProperty(PROPERTY_IDENTITY_NAME);
        }

        if (queryParameter.equals(IdentityType.ENABLED)) {
            property = this.config.getModelProperty(PROPERTY_IDENTITY_ENABLED);
        }

        if (queryParameter.equals(IdentityType.CREATED_DATE) || queryParameter.equals(IdentityType.CREATED_AFTER)
                || queryParameter.equals(IdentityType.CREATED_BEFORE)) {
            property = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED);
        }

        if (queryParameter.equals(IdentityType.EXPIRY_DATE) || queryParameter.equals(IdentityType.EXPIRY_AFTER)
                || queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
            property = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES);
        }

        return property;
    }

    public List<Predicate> getPredicates() {
        this.builder = this.entityManager.getCriteriaBuilder();

        String discriminator = this.config.getIdentityTypeDiscriminator(identityQuery.getIdentityType());

        this.predicates.add(builder.equal(root.get(this.config.getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                discriminator));

        for (Entry<QueryParameter, Object[]> entry : this.identityQuery.getParameters().entrySet()) {
            QueryParameter queryParameter = entry.getKey();
            Object[] parameterValues = entry.getValue();
            String propertyName = null;
            String comparationType = "eq";

            Property<Object> modelProperty = mapToModelProperty(queryParameter);

            if (modelProperty != null) {
                propertyName = modelProperty.getName();
            }

            if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                comparationType = "gt";
            }

            if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                comparationType = "lt";
            }

            if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                comparationType = "gt";
            }

            if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                comparationType = "lt";
            }

            if (modelProperty != null) {
                if (comparationType.equals("eq")) {
                    this.predicates.add(builder.equal(root.get(propertyName), parameterValues[0]));
                } else if (comparationType.equals("gt")) {
                    this.predicates.add(builder.greaterThan(root.<Date> get(propertyName), (Date) parameterValues[0]));
                } else if (comparationType.equals("lt")) {
                    this.predicates.add(builder.lessThan(root.<Date> get(propertyName), (Date) parameterValues[0]));
                }
            } else {
                if (queryParameter instanceof IdentityType.AttributeParameter) {
                    AttributeParameter customParameter = (AttributeParameter) queryParameter;

                    Subquery<?> subquery = criteria.subquery(this.config.getAttributeClass());
                    Root fromProject = subquery.from(this.config.getAttributeClass());
                    Subquery<?> select = subquery.select(fromProject.get(getAttributeIdentityProperty().getName()));

                    Predicate conjunction = builder.conjunction();

                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(getAttributeNameProperty().getName()), customParameter.getName()));
                    conjunction.getExpressions().add(
                            (fromProject.get(getAttributeValueProperty().getName()).in((Object[]) parameterValues)));

                    subquery.where(conjunction);

                    subquery.groupBy(subquery.getSelection()).having(
                            builder.equal(builder.count(subquery.getSelection()), parameterValues.length));

                    this.predicates.add(builder.in(root).value(subquery));
                }

                if (queryParameter.equals(Group.PARENT)) {
                    Join<Object, Object> join = root.join(this.config.getModelProperty(PROPERTY_PARENT_GROUP).getName());

                    this.predicates.add(builder.equal(join.get(this.config.getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                            parameterValues[0]));
                }
                
                if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE)) {
                    for (Object object : parameterValues) {
                        GroupRole groupRole = (GroupRole) object;

                        Property<Object> memberModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                        Property<Object> roleModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
                        Property<Object> groupModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                        Subquery<?> subquery = criteria.subquery(this.config.getMembershipClass());
                        Root fromProject = subquery.from(this.config.getMembershipClass());
                        Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                        Predicate conjunction = builder.conjunction();

                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(memberModelProperty.getName()), root));
                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(memberModelProperty.getName()), this.identityStore.lookupIdentityObjectById(groupRole.getMember())));
                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(roleModelProperty.getName()), this.identityStore.lookupIdentityObjectById(groupRole.getRole())));
                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(groupModelProperty.getName()), this.identityStore.lookupIdentityObjectById(groupRole.getGroup())));

                        subquery.where(conjunction);

                        this.predicates.add(builder.in(root).value(subquery));
                    }
                }
                
                if (queryParameter.equals(IdentityType.MEMBER_OF)) {
                    for (Object object : parameterValues) {
                        Property<Object> memberModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                        Property<Object> groupModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                        Subquery<?> subquery = criteria.subquery(this.config.getMembershipClass());
                        Root fromProject = subquery.from(this.config.getMembershipClass());
                        Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                        Predicate conjunction = builder.conjunction();

                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(memberModelProperty.getName()), root));
                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(groupModelProperty.getName()), this.identityStore.lookupIdentityObjectById(new SimpleGroup(object.toString()))));

                        subquery.where(conjunction);
                        
                        this.predicates.add(builder.in(root).value(subquery));
                    }
                }
                
                if (queryParameter.equals(IdentityType.HAS_ROLE)) {
                    for (Object object : parameterValues) {
                        String roleName = (String) parameterValues[0];

                        Property<Object> memberModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                        Property<Object> roleModelProperty = this.config.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);


                        Subquery<?> subquery = criteria.subquery(this.config.getMembershipClass());
                        Root fromProject = subquery.from(this.config.getMembershipClass());
                        Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                        Predicate conjunction = builder.conjunction();

                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(memberModelProperty.getName()), root));
                        conjunction.getExpressions().add(
                                builder.equal(fromProject.get(roleModelProperty.getName()), this.identityStore.lookupIdentityObjectById(new SimpleRole(object.toString()))));

                        subquery.where(conjunction);

                        this.predicates.add(builder.in(root).value(subquery));
                    }
                }
            }
        }

        return this.predicates;
    }

    public CriteriaQuery<?> getCriteria() {
        return this.criteria;
    }

    private Property<Object> getAttributeIdentityProperty() {
        return this.config.getModelProperty(PROPERTY_ATTRIBUTE_IDENTITY);
    }

    private Property<Object> getAttributeNameProperty() {
        return this.config.getModelProperty(PROPERTY_ATTRIBUTE_NAME);
    }

    private Property<Object> getAttributeValueProperty() {
        return this.config.getModelProperty(PROPERTY_ATTRIBUTE_VALUE);
    }

}
