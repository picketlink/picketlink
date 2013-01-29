package org.picketlink.common.properties;

import java.lang.reflect.Field;


public interface FieldProperty<V> extends Property<V> 
{
    Field getAnnotatedElement();
}
