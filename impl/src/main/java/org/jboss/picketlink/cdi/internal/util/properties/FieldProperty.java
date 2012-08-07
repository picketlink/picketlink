package org.jboss.picketlink.cdi.internal.util.properties;

import java.lang.reflect.Field;


public interface FieldProperty<V> extends Property<V> 
{
    Field getAnnotatedElement();
}
