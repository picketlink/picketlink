package org.picketlink.common.properties.query;

import org.picketlink.common.properties.FieldProperty;
import org.picketlink.common.properties.MethodProperty;
import org.picketlink.common.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * <p>This class is an adapter that encapsulates a {@link FieldProperty} and {@link MethodProperty} instance for a
 * property, giving a single view of a property and its related information independent of the member type: field or
 * method. The latter usually being a accessor or a mutator method.
 * </p>
 *
 * <p>This adapter is specially useful when checking for property's metadata independent of the member.
 * This is the case when checking the presence of annotations. As annotations can be defined in one of the members only,
 * if one of the members define the annotation the property itself has the annotation too.</p>
 *
 * @author Pedro Igor
 */
public class PropertyAdapter<V> implements Property<V> {

    private FieldProperty<V> fieldProperty;
    private MethodProperty<V> methodProperty;
    private Property<V> property;

    PropertyAdapter(FieldProperty fieldProperty) {
        if (fieldProperty == null) {
            throw new IllegalStateException("The field property must be defined.");
        }

        this.fieldProperty = fieldProperty;
    }

    PropertyAdapter(MethodProperty methodProperty) {
        if (methodProperty == null) {
            throw new IllegalStateException("The method property must be defined.");
        }

        this.methodProperty = methodProperty;
    }

    @Override
    public String getName() {
        return getProperty().getName();
    }

    @Override
    public Type getBaseType() {
        return getProperty().getBaseType();
    }

    @Override
    public Class<V> getJavaClass() {
        return getProperty().getJavaClass();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return getProperty().getAnnotatedElement();
    }

    @Override
    public Member getMember() {
        return getProperty().getMember();
    }

    @Override
    public V getValue(final Object instance) {
        return getProperty().getValue(instance);
    }

    @Override
    public void setValue(final Object instance, final V value) {
        getProperty().setValue(instance, value);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return getProperty().getDeclaringClass();
    }

    @Override
    public boolean isReadOnly() {
        return getProperty().isReadOnly();
    }

    @Override
    public void setAccessible() {
        getProperty().setAccessible();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
        if (isPropertyAnnotated(this.fieldProperty, annotation) || isPropertyAnnotated(this.methodProperty, annotation)) {
            return true;
        }

        return false;
    }

    private boolean isPropertyAnnotated(Property<V> property, Class<? extends Annotation> annotation) {
        return property != null && property.getAnnotatedElement() != null && property.isAnnotationPresent(annotation);
    }

    @Override
    public String toString() {
        return getProperty().toString();
    }

    @Override
    public int hashCode() {
        return getProperty().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getProperty().equals(obj);
    }

    void setProperty(final Property<V> property) {
        checkPropertySet(this.fieldProperty, property);
        checkPropertySet(this.methodProperty, property);

        if (FieldProperty.class.isInstance(property)) {
            this.fieldProperty = (FieldProperty<V>) property;
        } else if (MethodProperty.class.isInstance(property)) {
            this.methodProperty = (MethodProperty<V>) property;
        } else {
            throw new IllegalArgumentException("Invalid property type [" + property + "].");
        }
    }

    private Property<V> getProperty() {
        if (this.methodProperty != null) {
            return this.methodProperty;
        } else {
            return this.fieldProperty;
        }
    }

    private void checkPropertySet(final Property<V> currentProperty, final Property<V> newProperty) {
        if (newProperty == null) {
            throw new IllegalArgumentException("You must provide a property.");
        }

        if (currentProperty != null && currentProperty.getClass().equals(newProperty)) {
            if (!currentProperty.equals(property)) {
                throw new IllegalArgumentException("Property mismatch. Current [" + currentProperty + "]. Provided [" + newProperty + ".");
            }
            throw new IllegalArgumentException("Property [" + currentProperty + "] already set.");
        }
    }

}
