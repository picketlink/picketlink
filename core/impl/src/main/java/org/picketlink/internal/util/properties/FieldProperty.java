package org.picketlink.internal.util.properties;

import java.lang.reflect.Field;


public interface FieldProperty<V> extends Property<V> 
{
    Field getAnnotatedElement();
}
