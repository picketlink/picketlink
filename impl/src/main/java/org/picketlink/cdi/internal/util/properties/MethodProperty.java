package org.picketlink.cdi.internal.util.properties;

import java.lang.reflect.Method;


public interface MethodProperty<V> extends Property<V> 
{
    Method getAnnotatedElement();
}
