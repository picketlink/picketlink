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
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ENABLED;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_KEY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_PARTITION;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class IdentityTypeManager<T extends IdentityType> {

    private JPAIdentityStore store;

    public IdentityTypeManager(JPAIdentityStore store) {
        this.store = store;
    }

    void remove(Object identity, T identityType) {

    }

    public Object createIdentityInstance(Realm realm, T fromIdentityType) {
        Object identity = null;

        try {
            identity = getConfig().getIdentityClass().newInstance();

            populateIdentityInstance(realm, identity, (T) fromIdentityType);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return identity;
    }

    public T fromIdentityInstance(Realm realm, Object identity) {
        T identityType = createIdentityType(identity);

        identityType.setEnabled(getStore().getModelProperty(Boolean.class, identity, PROPERTY_IDENTITY_ENABLED));
        identityType.setExpirationDate(getStore().getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        identityType.setCreatedDate(getStore().getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));

        return identityType;
    }

    protected List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        
        if (queryParameter.equals(IdentityType.ENABLED)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_IDENTITY_ENABLED).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThan(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThan(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThan(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThan(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    (Date) parameterValues[0]));
        }
        
        if (queryParameter instanceof IdentityType.AttributeParameter) {
            AttributeParameter customParameter = (AttributeParameter) queryParameter;

            Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getAttributeClass());
            Root fromProject = subquery.from(getConfig().getAttributeClass());
            Subquery<?> select = subquery.select(fromProject.get(getAttributeIdentityProperty().getName()));

            Predicate conjunction = criteria.getBuilder().conjunction();

            conjunction.getExpressions().add(
                    criteria.getBuilder().equal(fromProject.get(getAttributeNameProperty().getName()), customParameter.getName()));
            conjunction.getExpressions().add(
                    (fromProject.get(getAttributeValueProperty().getName()).in((Object[]) parameterValues)));

            subquery.where(conjunction);

            subquery.groupBy(subquery.getSelection()).having(
                    criteria.getBuilder().equal(criteria.getBuilder().count(subquery.getSelection()), parameterValues.length));

            predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
        }

        return predicates;
    }
    
    protected abstract T createIdentityType(Object identity);
    protected abstract void fromIdentityType(Object toIdentity, T fromIdentityType);
    protected abstract AbstractBaseEvent raiseCreatedEvent(T fromIdentityType);
    protected abstract AbstractBaseEvent raiseUpdatedEvent(T fromIdentityType);
    protected abstract AbstractBaseEvent raiseDeletedEvent(T fromIdentityType);

    /**
     * <p>
     * Populates the given {@link Object} argument representing a Identity Class (from the config) with the information from the
     * specified {@link IdentityType}.
     * </p>
     * 
     * @param toIdentity
     * @param fromIdentityType
     */
    void populateIdentityInstance(Realm realm, Object toIdentity, T fromIdentityType) {
        // populate the common properties from IdentityType
        String identityDiscriminator = this.store.getIdentityDiscriminator(fromIdentityType.getClass());

        this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, identityDiscriminator, true);

        this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_KEY, fromIdentityType.getKey(), true);
        this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_ENABLED, fromIdentityType.isEnabled(), true);
        this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_CREATED, fromIdentityType.getCreatedDate(), true);
        this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_EXPIRES, fromIdentityType.getExpirationDate());

        if (realm != null) {
            this.store.setModelProperty(toIdentity, PROPERTY_IDENTITY_PARTITION, this.store.lookupPartitionObject(realm));
        }

        fromIdentityType(toIdentity, fromIdentityType);
    }

    protected JPAIdentityStoreConfiguration getConfig() {
        return this.store.getConfig();
    }

    protected JPAIdentityStore getStore() {
        return this.store;
    }

    private Property<Object> getAttributeIdentityProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_IDENTITY);
    }

    private Property<Object> getAttributeNameProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_NAME);
    }
    
    private Property<Object> getAttributeValueProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_VALUE);
    }
}
