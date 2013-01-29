package org.picketlink.common.properties.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A criteria that matches a property based on its type
 *
 * @see PropertyCriteria
 */
public class TypedPropertyCriteria implements PropertyCriteria 
{
    private final Class<?> propertyClass;

    public TypedPropertyCriteria(Class<?> propertyClass) 
    {
        this.propertyClass = propertyClass;
    }

    public boolean fieldMatches(Field f) 
    {
        return propertyClass.equals(f.getType());
    }

    public boolean methodMatches(Method m) 
    {
        return propertyClass.equals(m.getReturnType());
    }
}
