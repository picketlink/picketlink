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
package org.picketlink.idm.query;

import org.picketlink.idm.model.IdentityType;

/**
 * <p>The {@link IdentityQueryBuilder} is responsible for creating {@link org.picketlink.idm.query.IdentityQuery} instances and also
 * provide methods to create conditions, orderings, sorting, etc.</p>
 *
 * <p>Instances of this class are obtained from the {@link org.picketlink.idm.IdentityManager} as follows:</p>
 *
 * <pre>
 *      IdentityManager identityManager = getIdentityManager();
 *
 *      // here we get the query builder
 *      IdentityQueryBuilder builder = identityManager.getQueryBuilder();
 *
 *      // create a condition
 *      Condition condition = builder.equal(User.LOGIN_NAME, "john");
 *
 *      // create a query for a specific identity type using the previously created condition
 *      IdentityQuery query = builder.createIdentityQuery(User.class).where(condition);
 *
 *      // execute the query
 *      List<User> result = query.getResultList();
 * </pre>
 *
 * @author Pedro Igor
 */
public interface IdentityQueryBuilder {

    /**
     * <p>Create a condition for testing the whether the query parameter satisfies the given pattern..</p>
     *
     * @param parameter The query parameter.
     * @param pattern The pattern to match.
     *
     * @return
     */
    Condition like(QueryParameter parameter, String pattern);

    /**
     * <p>Create a condition for testing the arguments for equality.</p>
     *
     * @param parameter The query parameter.
     * @param value The value to compare.
     *
     * @return
     */
    Condition equal(QueryParameter parameter, Object value);

    /**
     * <p>Create a condition for testing whether the query parameter is grater than the given value..</p>
     *
     * @param parameter The query parameter.
     * @param x The value to compare.
     *
     * @return
     */
    Condition greaterThan(QueryParameter parameter, Object x);

    /**
     * <p>Create a condition for testing whether the query parameter is grater than or equal to the given value..</p>
     *
     * @param parameter The query parameter.
     * @param x The value to compare.
     *
     * @return
     */
    Condition greaterThanOrEqualTo(QueryParameter parameter, Object x);

    /**
     * <p>Create a condition for testing whether the query parameter is less than the given value..</p>
     *
     * @param parameter The query parameter.
     * @param x The value to compare.
     *
     * @return
     */
    Condition lessThan(QueryParameter parameter, Object x);

    /**
     * <p>Create a condition for testing whether the query parameter is less than or equal to the given value..</p>
     *
     * @param parameter The query parameter.
     * @param x The value to compare.
     *
     * @return
     */
    Condition lessThanOrEqualTo(QueryParameter parameter, Object x);

    /**
     * <p>Create a condition for testing whether the query parameter is between the given values.</p>
     *
     * @param parameter The query parameter.
     * @param x The first value.
     * @param x The second value.
     *
     * @return
     */
    Condition between(QueryParameter parameter, Object x, Object y);

    /**
     * <p>Create a condition for testing whether the query parameter is contained in a list of values.</p>
     *
     * @param parameter The query parameter.
     * @param values A list of values.
     *
     * @return
     */
    Condition in(QueryParameter parameter, Object... values);

    /**
     * <p>Create an ascending order for the given <code>parameter</code>. Once created, you can use it to sort the results of a
     * query.</p>
     *
     * @param parameter The query parameter to sort.
     *
     * @return
     */
    Sort asc(QueryParameter parameter);

    /**
     * <p>Create an descending order for the given <code>parameter</code>. Once created, you can use it to sort the results of a
     * query.</p>
     *
     * @param parameter The query parameter to sort.
     *
     * @return
     */
    Sort desc(QueryParameter parameter);

    /**
     * <p> Create an {@link org.picketlink.idm.query.IdentityQuery} that can be used to query for {@link
     * org.picketlink.idm.model.IdentityType} instances of a the given <code>identityType</code>. </p>
     *
     * @param identityType The type to search. If you provide the {@link org.picketlink.idm.model.IdentityType}
     * base interface any of its sub-types will be returned.
     *
     * @return
     */
    <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType);
}
