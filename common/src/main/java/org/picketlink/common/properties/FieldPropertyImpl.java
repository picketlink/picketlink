package org.picketlink.common.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.picketlink.common.reflection.Reflections;

import static org.picketlink.common.reflection.Reflections.getFieldValue;
import static org.picketlink.common.reflection.Reflections.setFieldValue;

/**
 * A bean property based on the value contained in a field
 *
 */
class FieldPropertyImpl<V> implements FieldProperty<V> 
{
    private final Field field;

    FieldPropertyImpl(Field field) 
    {
        this.field = field;
    }

    public String getName() 
    {
        return field.getName();
    }

    public Type getBaseType() 
    {
        return field.getGenericType();
    }

    public Field getAnnotatedElement() 
    {
        return field;
    }

    public Member getMember() 
    {
        return field;
    }

    @SuppressWarnings("unchecked")
    public Class<V> getJavaClass() 
    {
        return (Class<V>) field.getType();
    }

    public V getValue(Object instance) 
    {
        setAccessible();
        return getFieldValue(field, instance, getJavaClass());
    }

    public void setValue(Object instance, V value) 
    {
        setAccessible();
        setFieldValue(true, field, instance, value);
    }

    public Class<?> getDeclaringClass() 
    {
        return field.getDeclaringClass();
    }

    public boolean isReadOnly() 
    {
        return false;
    }

    public void setAccessible() 
    {
        Reflections.setAccessible(field);
    }

    @Override
    public String toString() 
    {
        return field.toString();
    }

    @Override
    public int hashCode() 
    {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object obj) 
    {
        return field.equals(obj);
    }
}
