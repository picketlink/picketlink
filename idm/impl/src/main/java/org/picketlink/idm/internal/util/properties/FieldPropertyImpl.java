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
package org.picketlink.idm.internal.util.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.picketlink.idm.internal.util.reflection.Reflections;

import static org.picketlink.idm.internal.util.reflection.Reflections.getFieldValue;
import static org.picketlink.idm.internal.util.reflection.Reflections.setFieldValue;

/**
 * A bean property based on the value contained in a field
 *
 */
class FieldPropertyImpl<V> implements FieldProperty<V> {
    private final Field field;

    FieldPropertyImpl(Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public Type getBaseType() {
        return field.getGenericType();
    }

    public Field getAnnotatedElement() {
        return field;
    }

    public Member getMember() {
        return field;
    }

    @SuppressWarnings("unchecked")
    public Class<V> getJavaClass() {
        return (Class<V>) field.getType();
    }

    public V getValue(Object instance) {
        setAccessible();
        return getFieldValue(field, instance, getJavaClass());
    }

    public void setValue(Object instance, V value) {
        setAccessible();
        setFieldValue(true, field, instance, value);
    }

    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public boolean isReadOnly() {
        return false;
    }

    public void setAccessible() {
        Reflections.setAccessible(field);
    }

    @Override
    public String toString() {
        return field.toString();
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return field.equals(obj);
    }
}
