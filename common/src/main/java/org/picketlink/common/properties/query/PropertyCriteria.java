package org.picketlink.common.properties.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>
 * A property criteria can be used to filter the properties found by a
 * {@link PropertyQuery}
 * </p>
 * <p/>
 * <p>
 * DeltaSpike provides a number of property queries (
 * {@link TypedPropertyCriteria}, {@link NamedPropertyCriteria} and
 * {@link AnnotatedPropertyCriteria}), or you can create a custom query by
 * implementing this interface.
 * </p>
 *
 * @see PropertyQuery#addCriteria(PropertyCriteria)
 * @see PropertyQueries
 * @see TypedPropertyCriteria
 * @see AnnotatedPropertyCriteria
 * @see NamedPropertyCriteria
 */
public interface PropertyCriteria 
{
    /**
     * Tests whether the specified field matches the criteria
     *
     * @param f
     * @return true if the field matches
     */
    boolean fieldMatches(Field f);

    /**
     * Tests whether the specified method matches the criteria
     *
     * @param m
     * @return true if the method matches
     */
    boolean methodMatches(Method m);
}
