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
package org.picketlink.idm.query.internal;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.internal.ContextualIdentityManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.Condition;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.Sort;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.StoreSelector;

/**
 * @author Pedro Igor
 */
public class DefaultQueryBuilder implements IdentityQueryBuilder {

    private final ContextualIdentityManager identityManager;
    private final StoreSelector storeSelector;

    public DefaultQueryBuilder(ContextualIdentityManager identityManager, StoreSelector storeSelector) {
        this.identityManager = identityManager;
        this.storeSelector = storeSelector;
    }

    @Override
    public Condition like(QueryParameter parameter, String pattern) {
        return new LikeCondition(parameter, pattern);
    }

    @Override
    public Condition equal(QueryParameter parameter, Object value) {
        return new EqualCondition(parameter, value);
    }

    @Override
    public Condition greaterThan(QueryParameter parameter, Object x) {
        throwExceptionIfNotComparable(x);
        return new GreaterThanCondition(parameter, (Comparable) x, false);
    }

    @Override
    public Condition greaterThanOrEqualTo(QueryParameter parameter, Object x) {
        throwExceptionIfNotComparable(x);
        return new GreaterThanCondition(parameter, (Comparable) x, true);
    }

    @Override
    public Condition lessThan(QueryParameter parameter, Object x) {
        throwExceptionIfNotComparable(x);
        return new LessThanCondition(parameter, (Comparable) x, false);
    }

    @Override
    public Condition lessThanOrEqualTo(QueryParameter parameter, Object x) {
        throwExceptionIfNotComparable(x);
        return new LessThanCondition(parameter, (Comparable) x, true);
    }

    @Override
    public Condition between(QueryParameter parameter, Object x, Object y) {
        throwExceptionIfNotComparable(x);
        throwExceptionIfNotComparable(y);
        return new BetweenCondition(parameter, (Comparable) x, (Comparable) y);
    }

    @Override
    public Condition in(QueryParameter parameter, Object... x) {
        return new InCondition(parameter, x);
    }

    @Override
    public Sort asc(QueryParameter parameter) {
        return new Sort(parameter, true);
    }

    @Override
    public Sort desc(QueryParameter parameter) {
        return new Sort(parameter, false);
    }

    @Override
    public <T extends IdentityType> IdentityQuery createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery(this, this.identityManager, identityType, storeSelector);
    }

    private void throwExceptionIfNotComparable(Object x) {
        if (!Comparable.class.isInstance(x)) {
            throw new IdentityManagementException("Query parameter value [" + x + "] must be " + Comparable.class + ".");
        }
    }
}
