package org.picketlink.cdi.internal.util.properties.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A criteria that matches a property based on its annotations
 *
 * @see PropertyCriteria
 */
public class AnnotatedPropertyCriteria implements PropertyCriteria 
{
    private final Class<? extends Annotation> annotationClass;

    public AnnotatedPropertyCriteria(Class<? extends Annotation> annotationClass) 
    {
        this.annotationClass = annotationClass;
    }

    public boolean fieldMatches(Field f) 
    {
        return f.isAnnotationPresent(annotationClass);
    }

    public boolean methodMatches(Method m) 
    {
        return m.isAnnotationPresent(annotationClass);
    }

}
