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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
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

        identityType.setEnabled(store.getModelProperty(Boolean.class, identity, PROPERTY_IDENTITY_ENABLED));
        identityType.setExpirationDate(store.getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        identityType.setCreatedDate(store.getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));

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
            identity = store.getConfig().getIdentityClass().newInstance();
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
        String identityDiscriminator = store.getConfig().getIdentityDiscriminator(fromIdentityType.getClass());

        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, identityDiscriminator, true);

        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_KEY, fromIdentityType.getKey(), true);
        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_ENABLED, fromIdentityType.isEnabled(), true);
        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_CREATED, fromIdentityType.getCreatedDate(), true);
        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_EXPIRES, fromIdentityType.getExpirationDate());

        if (realm != null) {
            store.setModelProperty(toIdentity, PROPERTY_IDENTITY_PARTITION, store.lookupPartitionObject(realm));
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
        JPAIdentityStoreConfiguration storeConfig = store.getConfig();
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (queryParameter.equals(IdentityType.ENABLED)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(storeConfig.getModelProperty(PROPERTY_IDENTITY_ENABLED).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(storeConfig.getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(storeConfig.getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(storeConfig.getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
            predicates.add(criteria.getBuilder().greaterThanOrEqualTo(
                    criteria.getRoot().<Date> get(storeConfig.getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(storeConfig.getModelProperty(PROPERTY_IDENTITY_CREATED).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
            predicates.add(criteria.getBuilder().lessThanOrEqualTo(
                    criteria.getRoot().<Date> get(storeConfig.getModelProperty(PROPERTY_IDENTITY_EXPIRES).getName()),
                    (Date) parameterValues[0]));
        }

        if (queryParameter instanceof IdentityType.AttributeParameter) {
            AttributeParameter customParameter = (AttributeParameter) queryParameter;

            Subquery<?> subquery = criteria.getCriteria().subquery(storeConfig.getAttributeClass());
            Root fromProject = subquery.from(storeConfig.getAttributeClass());
            Subquery<?> select = subquery.select(fromProject.get(storeConfig.getModelProperty(PROPERTY_ATTRIBUTE_IDENTITY).getName()));

            Predicate conjunction = criteria.getBuilder().conjunction();

            conjunction.getExpressions().add(
                    criteria.getBuilder().equal(fromProject.get(storeConfig.getModelProperty(PROPERTY_ATTRIBUTE_NAME).getName()),
                            customParameter.getName()));
            conjunction.getExpressions().add(
                    (fromProject.get(storeConfig.getModelProperty(PROPERTY_ATTRIBUTE_VALUE).getName()).in((Object[]) parameterValues)));

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
