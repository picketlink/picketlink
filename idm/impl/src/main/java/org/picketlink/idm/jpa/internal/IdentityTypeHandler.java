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

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.AttributedType.AttributeParameter;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.query.QueryParameter;

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

    protected JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    protected <P> P getModelPropertyValue(Class<P> propertyClass, Object instance, PropertyType propertyType) {
        @SuppressWarnings("unchecked")
        Property<P> property = (Property<P>) config.getModelProperty(propertyType);
        return property == null ? null : property.getValue(instance);
    }

    protected void setModelPropertyValue(Object instance, PropertyType propertyType, Object value) {
        setModelPropertyValue(instance, propertyType, value, false);
    }

    protected void setModelPropertyValue(Object instance, PropertyType propertyType, Object value, boolean required) {
        if (config.isModelPropertySet(propertyType)) {
           config.getModelProperty(propertyType).setValue(instance, value);
        } else if (required) {
            throw new IdentityManagementException("Model property [" + propertyType.name() + "] has not been configured.");
        }
    }

    /**
     * <p>
     * Creates a {@link IdentityType} instance using the information from the given Identity Class instance. This method already
     * provides the mapping for the common properties for all {@link IdentityType} types.
     * </p>
     * 
     * @param realm
     * @param identity
     * @return
     */
    public T createIdentityType(Realm realm, Object identity, JPAIdentityStore store) {
        T identityType = doCreateIdentityType(identity, store);

        identityType.setEnabled(getModelPropertyValue(Boolean.class, identity, PropertyType.IDENTITY_ENABLED));
        identityType.setExpirationDate(getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_EXPIRY_DATE));
        identityType.setCreatedDate(getModelPropertyValue(Date.class, identity, PropertyType.IDENTITY_CREATION_DATE));

        return identityType;
    }
    
    /**
     * <p>
     * Creates a Identity Class instance using the information from the given {@link IdentityType}.
     * </p>
     * 
     * @param realm
     * @param fromIdentityType
     * @return
     */
    public Object createIdentityInstance(Realm realm, T fromIdentityType, JPAIdentityStore store) {
        Object identity = null;

        try {
            identity = getConfig().getIdentityClass().newInstance();
            populateIdentityInstance(realm, identity, fromIdentityType, store);
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
    protected void populateIdentityInstance(Realm realm, Object toIdentity, T fromIdentityType, JPAIdentityStore store) {
        // populate the common properties from IdentityType
        String identityDiscriminator = getConfig().getIdentityDiscriminator(fromIdentityType.getClass());

        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_DISCRIMINATOR, identityDiscriminator, true);

        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_KEY, fromIdentityType.getKey(), true);
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ENABLED, fromIdentityType.isEnabled(), true);
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_CREATION_DATE, fromIdentityType.getCreatedDate(), true);
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_EXPIRY_DATE, fromIdentityType.getExpirationDate());

        if (realm != null) {
            setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupPartitionObject(realm));
        }

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
     * @param queryParameter
     * @param parameterValues
     * @param criteria
     * @return
     */
    public List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (queryParameter.equals(IdentityType.ENABLED)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ENABLED).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_CREATION_DATE).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(getConfig().getModelProperty(PropertyType.IDENTITY_EXPIRY_DATE).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter instanceof IdentityType.AttributeParameter) {
            AttributeParameter customParameter = (AttributeParameter) queryParameter;

            Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getAttributeClass());
            Root fromProject = subquery.from(getConfig().getAttributeClass());
            Subquery<?> select = subquery.select(fromProject.get(getConfig().getModelProperty(
                    PropertyType.ATTRIBUTE_IDENTITY).getName()));

            Predicate conjunction = criteria.getBuilder().conjunction();

            conjunction.getExpressions().add(
                    criteria.getBuilder().equal(fromProject.get(getConfig().getModelProperty(
                            PropertyType.ATTRIBUTE_NAME).getName()), customParameter.getName()));
            conjunction.getExpressions().add(
                    (fromProject.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE).getName())
                            .in((Object[]) parameterValues)));

            subquery.where(conjunction);

            subquery.groupBy(subquery.getSelection()).having(
                    criteria.getBuilder().equal(criteria.getBuilder().count(subquery.getSelection()), parameterValues.length));

            predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
        }

        return predicates;
    }

    /**
     * <p>Subclasses should override this method to create a specific {@link IdentityType} given the provided Identity Class instance.</p>
     * 
     * @param identity
     * @return
     */
    protected abstract T doCreateIdentityType(Object identity, JPAIdentityStore store);

    /**
     * <p>Subclasses should override this method to populate the given Identity Class instance with the specific information for a given {@link IdentityType}.</p>
     * 
     * @param toIdentity
     * @param fromIdentityType
     */
    protected abstract void doPopulateIdentityInstance(Object toIdentity, T fromIdentityType, JPAIdentityStore store);

    protected abstract AbstractBaseEvent raiseCreatedEvent(T fromIdentityType, JPAIdentityStore store);

    protected abstract AbstractBaseEvent raiseUpdatedEvent(T fromIdentityType, JPAIdentityStore store);

    protected abstract AbstractBaseEvent raiseDeletedEvent(T fromIdentityType, JPAIdentityStore store);

}
