/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.common.reflection;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

/**
 * Utility class for working with JDK Reflection and also CDI's
 * {@link Annotated} metadata.
 *
 */
public class Reflections 
{
    /**
     * An empty array of type {@link Annotation}, useful converting lists to
     * arrays.
     */
    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    /**
     * An empty array of type {@link Object}, useful for converting lists to
     * arrays.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final Type[] EMPTY_TYPES = {};

    public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];
    
    private Reflections() 
    {
    }    

    /**
     * <p>
     * Perform a runtime cast. Similar to {@link Class#cast(Object)}, but useful
     * when you do not have a {@link Class} object for type you wish to cast to.
     * </p>
     * <p/>
     * <p>
     * {@link Class#cast(Object)} should be used if possible
     * </p>
     *
     * @param <T> the type to cast to
     * @param obj the object to perform the cast on
     * @return the casted object
     * @throws ClassCastException if the type T is not a subtype of the object
     * @see Class#cast(Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) 
    {
        return (T) obj;
    }

    /**
     * Get all the declared fields on the class hierarchy. This <b>will</b>
     * return overridden fields.
     *
     * @param clazz The class to search
     * @return the set of all declared fields or an empty set if there are none
     */
    public static Set<Field> getAllDeclaredFields(Class<?> clazz) 
    {
        HashSet<Field> fields = new HashSet<Field>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            for (Field a : c.getDeclaredFields()) 
            {
                fields.add(a);
            }
        }
        return fields;
    }

    /**
     * Search the class hierarchy for a field with the given name. Will return
     * the nearest match, starting with the class specified and searching up the
     * hierarchy.
     *
     * @param clazz The class to search
     * @param name  The name of the field to search for
     * @return The field found, or null if no field is found
     */
    public static Field findDeclaredField(Class<?> clazz, String name) 
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            try 
            {
                return c.getDeclaredField(name);
            } 
            catch (NoSuchFieldException e) 
            {
                // No-op, we continue looking up the class hierarchy
            }
        }
        return null;
    }

    /**
     * Search the annotatedType for the field, returning the
     * {@link AnnotatedField}
     *
     * @param annotatedType The annotatedType to search
     * @param field         the field to search for
     * @return The {@link AnnotatedField} found, or null if no field is found
     */
    public static <X> AnnotatedField<? super X> getField(AnnotatedType<X> annotatedType, Field field) 
    {
        for (AnnotatedField<? super X> annotatedField : annotatedType.getFields()) 
        {
            if (annotatedField.getDeclaringType().getJavaClass().equals(
                    field.getDeclaringClass()) && annotatedField.getJavaMember().getName().equals(field.getName())) 
            {
                return annotatedField;
            }
        }
        return null;
    }

    /**
     * Search for annotations with the specified meta annotation type
     *
     * @param annotations        The annotation set to search
     * @param metaAnnotationType The type of the meta annotation to search for
     * @return The set of annotations with the specified meta annotation, or an
     *         empty set if none are found
     */
    public static Set<Annotation> getAnnotationsWithMetaAnnotation(
            Set<Annotation> annotations, Class<? extends Annotation> metaAnnotationType) 
    {
        Set<Annotation> set = new HashSet<Annotation>();
        for (Annotation annotation : annotations) 
        {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) 
            {
                set.add(annotation);
            }
        }
        return set;
    }

    /**
     * Extract any qualifiers from the set of annotations
     *
     * @param annotations The set of annotations to search
     * @param beanManager The beanManager to use to establish if an annotation is
     *                    a qualifier
     * @return The qualifiers present in the set, or an empty set if there are
     *         none
     */
    public static Set<Annotation> getQualifiers(Set<Annotation> annotations, BeanManager beanManager) 
    {
        Set<Annotation> set = new HashSet<Annotation>();
        for (Annotation annotation : annotations) 
        {
            if (beanManager.isQualifier(annotation.annotationType())) 
            {
                set.add(annotation);
            }
        }
        return set;
    }

    /**
     * Determine if a method exists in a specified class hierarchy
     *
     * @param clazz The class to search
     * @param name  The name of the method
     * @return true if a method is found, otherwise false
     */
    public static boolean methodExists(Class<?> clazz, String name) 
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            for (Method m : c.getDeclaredMethods()) 
            {
                if (m.getName().equals(name)) 
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all the declared methods on the class hierarchy. This <b>will</b>
     * return overridden methods.
     *
     * @param clazz The class to search
     * @return the set of all declared methods or an empty set if there are none
     */
    public static Set<Method> getAllDeclaredMethods(Class<?> clazz) 
    {
        HashSet<Method> methods = new HashSet<Method>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            for (Method a : c.getDeclaredMethods()) 
            {
                methods.add(a);
            }
        }
        return methods;
    }

    /**
     * Search the class hierarchy for a method with the given name and arguments.
     * Will return the nearest match, starting with the class specified and
     * searching up the hierarchy.
     *
     * @param clazz The class to search
     * @param name  The name of the method to search for
     * @param args  The arguments of the method to search for
     * @return The method found, or null if no method is found
     */
    public static Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... args) 
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            try 
            {
                return c.getDeclaredMethod(name, args);
            } 
            catch (NoSuchMethodException e) 
            {
                // No-op, continue the search
            }
        }
        return null;
    }

    /**
     * Search the class hierarchy for a constructor with the given arguments.
     * Will return the nearest match, starting with the class specified and
     * searching up the hierarchy.
     *
     * @param clazz The class to search
     * @param args  The arguments of the constructor to search for
     * @return The constructor found, or null if no constructor is found
     */
    public static Constructor<?> findDeclaredConstructor(Class<?> clazz, Class<?>... args) 
    {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            try 
            {
                return c.getDeclaredConstructor(args);
            } 
            catch (NoSuchMethodException e) 
            {
                // No-op, continue the search
            }
        }
        return null;
    }

    /**
     * Get all the declared constructors on the class hierarchy. This <b>will</b>
     * return overridden constructors.
     *
     * @param clazz The class to search
     * @return the set of all declared constructors or an empty set if there are
     *         none
     */
    public static Set<Constructor<?>> getAllDeclaredConstructors(Class<?> clazz) 
    {
        HashSet<Constructor<?>> constructors = new HashSet<Constructor<?>>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) 
        {
            for (Constructor<?> constructor : c.getDeclaredConstructors()) 
            {
                constructors.add(constructor);
            }
        }
        return constructors;
    }

    /**
     * Get the type of the member
     *
     * @param member The member
     * @return The type of the member
     * @throws UnsupportedOperationException if the member is not a field,
     *                                       method, or constructor
     */
    public static Class<?> getMemberType(Member member) 
    {
        if (member instanceof Field) 
        {
            return ((Field) member).getType();
        } 
        else if (member instanceof Method) 
        {
            return ((Method) member).getReturnType();
        } 
        else if (member instanceof Constructor<?>) 
        {
            return ((Constructor<?>) member).getDeclaringClass();
        } 
        else 
        {
            throw new UnsupportedOperationException("Cannot operate on a member of type " + member.getClass());
        }
    }

    /**
     * <p>
     * Loads and initializes a class for the given name.
     * </p>
     * <p/>
     * <p>
     * If the Thread Context Class Loader is available, it will be used,
     * otherwise the classloader used to load {@link Reflections} will be used
     * </p>
     * <p/>
     * <p>
     * It is also possible to specify additional classloaders to attempt to load
     * the class with. If the first attempt fails, then these additional loaders
     * are tried in order.
     * </p>
     *
     * @param name    the name of the class to load
     * @param loaders additional classloaders to use to attempt to load the class
     * @return the class object
     * @throws ClassNotFoundException if the class cannot be found
     */
    public static Class<?> classForName(String name, ClassLoader... loaders) throws ClassNotFoundException 
    {
        try 
        {
            if (Thread.currentThread().getContextClassLoader() != null) 
            {
                return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
            } 
            else 
            {
                return Class.forName(name);
            }
        } 
        catch (ClassNotFoundException e) 
        {
            for (ClassLoader l : loaders) 
            {
                try 
                {
                    return Class.forName(name, true, l);
                } 
                catch (ClassNotFoundException ex) 
                {

                }
            }
        }
        if (Thread.currentThread().getContextClassLoader() != null) 
        {
            throw new ClassNotFoundException("Could not load class " + name + 
                    " with the context class loader " + Thread.currentThread().getContextClassLoader().toString() + 
                    " or any of the additional ClassLoaders: " + Arrays.toString(loaders));
        } 
        else 
        {
            throw new ClassNotFoundException("Could not load class " + name + 
                    " using Class.forName or using any of the additional ClassLoaders: " + 
                    Arrays.toString(loaders));
        }
    }

    private static String buildInvokeMethodErrorMessage(Method method, Object obj, Object... args) 
    {
        StringBuilder message = new StringBuilder(
                String.format("Exception invoking method [%s] on object [%s], using arguments [", 
                        method.getName(), obj));
        if (args != null)
        {
            for (int i = 0; i < args.length; i++)
            {
                message.append((i > 0 ? "," : "") + args[i]);
            }
        }
        message.append("]");
        return message.toString();
    }

    /**
     * Set the accessibility flag on the {@link AccessibleObject} as described in
     * {@link AccessibleObject#setAccessible(boolean)} within the context of
     * a {@link PrivilegedAction}.
     *
     * @param <A>    member the accessible object type
     * @param member the accessible object
     * @return the accessible object after the accessible flag has been altered
     */
    public static <A extends AccessibleObject> A setAccessible(A member) 
    {
        AccessController.doPrivileged(new SetAccessiblePriviligedAction(member));
        return member;
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional
     * arguments included in this method as arguments to the specified method.
     * </p>
     * <p/>
     * <p>This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the
     * expected return type set to {@link Object} and no change to the method's accessibility.</p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static Object invokeMethod(Method method, Object instance, Object... args) 
    {
        return invokeMethod(false, method, Object.class, instance, args);
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional
     * arguments included in this method as arguments to the specified method.
     * </p>
     * <p/>
     * <p>
     * This method attempts to set the accessible flag of the method in a
     * {@link PrivilegedAction} before invoking the method if the first argument
     * is true.
     * </p>
     * <p/>
     * <p>This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the
     * expected return type set to {@link Object}.</p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static Object invokeMethod(boolean setAccessible, Method method, Object instance, Object... args) 
    {
        return invokeMethod(setAccessible, method, Object.class, instance, args);
    }

    /**
     * <p>
     * Invoke the specified method on the provided instance, passing any additional
     * arguments included in this method as arguments to the specified method.
     * </p>
     * <p/>
     * <p>This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#invokeMethod(boolean, Method, Class, Object, Object...)}, with the
     * expected return type set to {@link Object} and honoring the accessibility of
     * the method.</p>
     *
     * @see Reflections#invokeMethod(boolean, Method, Class, Object, Object...)
     * @see Method#invoke(Object, Object...)
     */
    public static <T> T invokeMethod(Method method, Class<T> expectedReturnType, Object instance, Object... args) 
    {
        return invokeMethod(false, method, expectedReturnType, instance, args);
    }

    /**
     * <p>
     * Invoke the method on the instance, with any arguments specified, casting
     * the result of invoking the method to the expected return type.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Method#invoke(Object, Object...)}, converting the
     * checked exceptions that {@link Method#invoke(Object, Object...)} specifies
     * to runtime exceptions.
     * </p>
     * <p/>
     * <p>
     * If instructed, this method attempts to set the accessible flag of the method in a
     * {@link PrivilegedAction} before invoking the method.
     * </p>
     *
     * @param setAccessible flag indicating whether method should first be set as
     *                      accessible
     * @param method        the method to invoke
     * @param instance      the instance to invoke the method
     * @param args          the arguments to the method
     * @return the result of invoking the method, or null if the method's return
     *         type is void
     * @throws RuntimeException            if this <code>Method</code> object enforces Java
     *                                     language access control and the underlying method is
     *                                     inaccessible or if the underlying method throws an exception or
     *                                     if the initialization provoked by this method fails.
     * @throws IllegalArgumentException    if the method is an instance method and
     *                                     the specified <code>instance</code> argument is not an instance
     *                                     of the class or interface declaring the underlying method (or
     *                                     of a subclass or implementor thereof); if the number of actual
     *                                     and formal parameters differ; if an unwrapping conversion for
     *                                     primitive arguments fails; or if, after possible unwrapping, a
     *                                     parameter value cannot be converted to the corresponding formal
     *                                     parameter type by a method invocation conversion.
     * @throws NullPointerException        if the specified <code>instance</code> is
     *                                     null and the method is an instance method.
     * @throws ClassCastException          if the result of invoking the method cannot be
     *                                     cast to the expectedReturnType
     * @throws ExceptionInInitializerError if the initialization provoked by this
     *                                     method fails.
     * @see Method#invoke(Object, Object...)
     */
    public static <T> T invokeMethod(boolean setAccessible, Method method, 
            Class<T> expectedReturnType, Object instance, Object... args) 
    {
        if (setAccessible && !method.isAccessible()) 
        {
            setAccessible(method);
        }

        try 
        {
            return expectedReturnType.cast(method.invoke(instance, args));
        } 
        catch (IllegalAccessException ex) 
        {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        } 
        catch (IllegalArgumentException ex) 
        {
            throw new IllegalArgumentException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        } 
        catch (InvocationTargetException ex) 
        {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex.getCause());
        } 
        catch (NullPointerException ex) 
        {
            NullPointerException ex2 = new NullPointerException(buildInvokeMethodErrorMessage(method, instance, args));
            ex2.initCause(ex.getCause());
            throw ex2;
        } 
        catch (ExceptionInInitializerError e) 
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(
                    buildInvokeMethodErrorMessage(method, instance, args));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    /**
     * <p>
     * Set the value of a field on the instance to the specified value.
     * </p>
     * <p/>
     * <p>This method provides the same functionality and throws the same exceptions as
     * {@link Reflections#setFieldValue(boolean, Method, Class, Object, Object...)}, honoring
     * the accessibility of the field.</p>
     */
    public static void setFieldValue(Field field, Object instance, Object value) 
    {
        setFieldValue(false, field, instance, value);
    }

    /**
     * <p>
     * Sets the value of a field on the instance to the specified value.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Field#set(Object, Object)}, converting the
     * checked exceptions that {@link Field#set(Object, Object)} specifies to
     * runtime exceptions.
     * </p>
     * <p/>
     * <p>
     * If instructed, this method attempts to set the accessible flag of the method in a
     * {@link PrivilegedAction} before invoking the method.
     * </p>
     *
     * @param field    the field on which to operate, or null if the field is static
     * @param instance the instance on which the field value should be set upon
     * @param value    the value to set the field to
     * @throws RuntimeException            if the underlying field is inaccessible.
     * @throws IllegalArgumentException    if the specified <code>instance</code> is not an
     *                                     instance of the class or interface declaring the underlying
     *                                     field (or a subclass or implementor thereof), or if an
     *                                     unwrapping conversion fails.
     * @throws NullPointerException        if the specified <code>instance</code> is null and the field
     *                                     is an instance field.
     * @throws ExceptionInInitializerError if the initialization provoked by this
     *                                     method fails.
     * @see Field#set(Object, Object)
     */
    public static void setFieldValue(boolean setAccessible, Field field, Object instance, Object value) 
    {
        if (setAccessible && !field.isAccessible()) 
        {
            setAccessible(field);
        }

        try 
        {
            field.set(instance, value);
        } 
        catch (IllegalAccessException e) 
        {
            throw new RuntimeException(buildSetFieldValueErrorMessage(field, instance, value), e);
        } 
        catch (NullPointerException ex) 
        {
            NullPointerException ex2 = new NullPointerException(buildSetFieldValueErrorMessage(field, instance, value));
            ex2.initCause(ex.getCause());
            throw ex2;
        } 
        catch (ExceptionInInitializerError e) 
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(
                    buildSetFieldValueErrorMessage(field, instance, value));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    private static String buildSetFieldValueErrorMessage(Field field, Object obj, Object value) 
    {
        return String.format("Exception setting [%s] field on object [%s] to value [%s]", field.getName(), obj, value);
    }

    private static String buildGetFieldValueErrorMessage(Field field, Object obj) 
    {
        return String.format("Exception reading [%s] field from object [%s].", field.getName(), obj);
    }

    public static Object getFieldValue(Field field, Object instance) 
    {
        return getFieldValue(field, instance, Object.class);
    }

    /**
     * <p>
     * Get the value of the field, on the specified instance, casting the value
     * of the field to the expected type.
     * </p>
     * <p/>
     * <p>
     * This method wraps {@link Field#get(Object)}, converting the checked
     * exceptions that {@link Field#get(Object)} specifies to runtime exceptions.
     * </p>
     *
     * @param <T>          the type of the field's value
     * @param field        the field to operate on
     * @param instance     the instance from which to retrieve the value
     * @param expectedType the expected type of the field's value
     * @return the value of the field
     * @throws RuntimeException            if the underlying field is inaccessible.
     * @throws IllegalArgumentException    if the specified <code>instance</code> is not an
     *                                     instance of the class or interface declaring the underlying
     *                                     field (or a subclass or implementor thereof).
     * @throws NullPointerException        if the specified <code>instance</code> is null and the field
     *                                     is an instance field.
     * @throws ExceptionInInitializerError if the initialization provoked by this
     *                                     method fails.
     */
    public static <T> T getFieldValue(Field field, Object instance, Class<T> expectedType) 
    {
        try 
        {
            return Reflections.cast(field.get(instance));
        } 
        catch (IllegalAccessException e) 
        {
            throw new RuntimeException(buildGetFieldValueErrorMessage(field, instance), e);
        } 
        catch (NullPointerException ex) 
        {
            NullPointerException ex2 = new NullPointerException(buildGetFieldValueErrorMessage(field, instance));
            ex2.initCause(ex.getCause());
            throw ex2;
        } 
        catch (ExceptionInInitializerError e) 
        {
            ExceptionInInitializerError e2 = new ExceptionInInitializerError(
                    buildGetFieldValueErrorMessage(field, instance));
            e2.initCause(e.getCause());
            throw e2;
        }
    }

    /**
     * Extract the raw type, given a type.
     *
     * @param <T>  the type
     * @param type the type to extract the raw type from
     * @return the raw type, or null if the raw type cannot be determined.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getRawType(Type type) 
    {
        if (type instanceof Class<?>) 
        {
            return (Class<T>) type;
        } 
        else if (type instanceof ParameterizedType) 
        {
            if (((ParameterizedType) type).getRawType() instanceof Class<?>) 
            {
                return (Class<T>) ((ParameterizedType) type).getRawType();
            }
        }
        return null;
    }

    /**
     * Check if a class is serializable.
     *
     * @param clazz The class to check
     * @return true if the class implements serializable or is a primitive
     */
    public static boolean isSerializable(Class<?> clazz) 
    {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }


    public static Map<Class<?>, Type> buildTypeMap(Set<Type> types) 
    {
        Map<Class<?>, Type> map = new HashMap<Class<?>, Type>();
        for (Type type : types) 
        {
            if (type instanceof Class<?>) 
            {
                map.put((Class<?>) type, type);
            } 
            else if (type instanceof ParameterizedType) 
            {
                if (((ParameterizedType) type).getRawType() instanceof Class<?>) 
                {
                    map.put((Class<?>) ((ParameterizedType) type).getRawType(), type);
                }
            } 
            else if (type instanceof TypeVariable<?>) 
            {

            }
        }
        return map;
    }

    public static boolean isCacheable(Set<Annotation> annotations) 
    {
        for (Annotation qualifier : annotations) 
        {
            Class<?> clazz = qualifier.getClass();
            if (clazz.isAnonymousClass() || (clazz.isMemberClass() && isStatic(clazz))) 
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isCacheable(Annotation[] annotations) 
    {
        for (Annotation qualifier : annotations) 
        {
            Class<?> clazz = qualifier.getClass();
            if (clazz.isAnonymousClass() || (clazz.isMemberClass() && isStatic(clazz))) 
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the property name from a getter method.
     * <p/>
     * We extend JavaBean conventions, allowing the getter method to have parameters
     *
     * @param method The getter method
     * @return The name of the property. Returns null if method wasn't JavaBean
     *         getter-styled
     */
    public static String getPropertyName(Method method) 
    {
        String methodName = method.getName();
        if (methodName.matches("^(get).*")) 
        {
            return Introspector.decapitalize(methodName.substring(3));
        } 
        else if (methodName.matches("^(is).*")) 
        {
            return Introspector.decapitalize(methodName.substring(2));
        } 
        else 
        {
            return null;
        }

    }

    /**
     * Checks if class is final
     *
     * @param clazz The class to check
     * @return True if final, false otherwise
     */
    public static boolean isFinal(Class<?> clazz) 
    {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public static int getNesting(Class<?> clazz) 
    {
        if (clazz.isMemberClass() && !isStatic(clazz)) 
        {
            return 1 + getNesting(clazz.getDeclaringClass());
        } 
        else 
        {
            return 0;
        }
    }

    /**
     * Checks if member is final
     *
     * @param member The member to check
     * @return True if final, false otherwise
     */
    public static boolean isFinal(Member member) 
    {
        return Modifier.isFinal(member.getModifiers());
    }

    /**
     * Checks if member is private
     *
     * @param member The member to check
     * @return True if final, false otherwise
     */
    public static boolean isPrivate(Member member) 
    {
        return Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Checks if type or member is final
     *
     * @param type Type or member
     * @return True if final, false otherwise
     */
    public static boolean isTypeOrAnyMethodFinal(Class<?> type) 
    {
        return getNonPrivateFinalMethodOrType(type) != null;
    }

    public static Object getNonPrivateFinalMethodOrType(Class<?> type) 
    {
        if (isFinal(type)) 
        {
            return type;
        }
        for (Method method : type.getDeclaredMethods()) 
        {
            if (isFinal(method) && !isPrivate(method)) 
            {
                return method;
            }
        }
        return null;
    }

    public static boolean isPackagePrivate(int mod) 
    {
        return !(Modifier.isPrivate(mod) || Modifier.isProtected(mod) || Modifier.isPublic(mod));
    }

    /**
     * Checks if type is static
     *
     * @param type Type to check
     * @return True if static, false otherwise
     */
    public static boolean isStatic(Class<?> type) 
    {
        return Modifier.isStatic(type.getModifiers());
    }

    /**
     * Checks if member is static
     *
     * @param member Member to check
     * @return True if static, false otherwise
     */
    public static boolean isStatic(Member member) 
    {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isTransient(Member member) 
    {
        return Modifier.isTransient(member.getModifiers());
    }

    /**
     * Checks if a method is abstract
     *
     * @param method
     * @return
     */
    public static boolean isAbstract(Method method)
    {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Gets the actual type arguments of a class
     *
     * @param clazz The class to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Class<?> clazz) 
    {
        Type type = new HierarchyDiscovery(clazz).getResolvedType();
        if (type instanceof ParameterizedType) 
        {
            return ((ParameterizedType) type).getActualTypeArguments();
        } 
        else 
        {
            return EMPTY_TYPES;
        }
    }

    /**
     * Gets the actual type arguments of a Type
     *
     * @param type The type to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Type type) 
    {
        Type resolvedType = new HierarchyDiscovery(type).getResolvedType();
        if (resolvedType instanceof ParameterizedType) 
        {
            return ((ParameterizedType) resolvedType).getActualTypeArguments();
        } 
        else 
        {
            return EMPTY_TYPES;
        }
    }

    /**
     * Checks if raw type is array type
     *
     * @param rawType The raw type to check
     * @return True if array, false otherwise
     */
    public static boolean isArrayType(Class<?> rawType) 
    {
        return rawType.isArray();
    }

    /**
     * Checks if type is parameterized type
     *
     * @param type The type to check
     * @return True if parameterized, false otherwise
     */
    public static boolean isParameterizedType(Class<?> type) 
    {
        return type.getTypeParameters().length > 0;
    }

    public static boolean isParamerterizedTypeWithWildcard(Class<?> type) 
    {
        if (isParameterizedType(type)) 
        {
            return containsWildcards(type.getTypeParameters());
        } 
        else 
        {
            return false;
        }
    }

    public static boolean containsWildcards(Type[] types) 
    {
        for (Type type : types) 
        {
            if (type instanceof WildcardType) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the bindingType to make sure the annotation was declared properly
     * as a binding type (annotated with @BindingType) and that it has a runtime
     * retention policy.
     *
     * @param binding The binding type to check
     * @return true only if the annotation is really a binding type
     */
    @Deprecated
    // TODO Replace usage of this with metadatacache
    public static boolean isBindings(Annotation binding) 
    {
        boolean isBindingAnnotation = false;
        if (binding.annotationType().isAnnotationPresent(Qualifier.class) && 
                binding.annotationType().isAnnotationPresent(Retention.class) && 
                binding.annotationType().getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME)) 
        {
            isBindingAnnotation = true;
        }
        return isBindingAnnotation;
    }

    /**
     * Check the assignability of one type to another, taking into account the
     * actual type arguements
     *
     * @param rawType1             the raw type of the class to check
     * @param actualTypeArguments1 the actual type arguements to check, or an
     *                             empty array if not a parameterized type
     * @param rawType2             the raw type of the class to check
     * @param actualTypeArguments2 the actual type arguements to check, or an
     *                             empty array if not a parameterized type
     * @return
     */
    public static boolean isAssignableFrom(Class<?> rawType1, Type[] actualTypeArguments1, 
            Class<?> rawType2, Type[] actualTypeArguments2) 
    {
        return Types.boxedClass(rawType1).isAssignableFrom(Types.boxedClass(rawType2)) && 
                isAssignableFrom(actualTypeArguments1, actualTypeArguments2);
    }

    public static boolean matches(Class<?> rawType1, Type[] actualTypeArguments1, 
            Class<?> rawType2, Type[] actualTypeArguments2) 
    {
        return Types.boxedClass(rawType1).equals(Types.boxedClass(rawType2)) && 
                isAssignableFrom(actualTypeArguments1, actualTypeArguments2);
    }

    public static boolean isAssignableFrom(Type[] actualTypeArguments1, Type[] actualTypeArguments2) 
    {
        for (int i = 0; i < actualTypeArguments1.length; i++) 
        {
            Type type1 = actualTypeArguments1[i];
            Type type2 = Object.class;
            if (actualTypeArguments2.length > i) 
            {
                type2 = actualTypeArguments2[i];
            }
            if (!isAssignableFrom(type1, type2)) 
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isAssignableFrom(Type type1, Set<? extends Type> types2) 
    {
        for (Type type2 : types2) 
        {
            if (isAssignableFrom(type1, type2)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Type type1, Set<? extends Type> types2) 
    {
        for (Type type2 : types2) 
        {
            if (matches(type1, type2)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Type type1, Type[] types2) 
    {
        for (Type type2 : types2) 
        {
            if (isAssignableFrom(type1, type2)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Type type1, Type type2) 
    {
        if (type1 instanceof Class<?>) 
        {
            Class<?> clazz = (Class<?>) type1;
            if (isAssignableFrom(clazz, EMPTY_TYPES, type2)) 
            {
                return true;
            }
        }
        if (type1 instanceof ParameterizedType) 
        {
            ParameterizedType parameterizedType1 = (ParameterizedType) type1;
            if (parameterizedType1.getRawType() instanceof Class<?>) 
            {
                if (isAssignableFrom((Class<?>) parameterizedType1.getRawType(), 
                        parameterizedType1.getActualTypeArguments(), type2)) 
                {
                    return true;
                }
            }
        }
        if (type1 instanceof WildcardType) 
        {
            WildcardType wildcardType = (WildcardType) type1;
            if (isTypeBounded(type2, wildcardType.getLowerBounds(), wildcardType.getUpperBounds())) 
            {
                return true;
            }
        }
        if (type2 instanceof WildcardType) 
        {
            WildcardType wildcardType = (WildcardType) type2;
            if (isTypeBounded(type1, wildcardType.getUpperBounds(), wildcardType.getLowerBounds())) 
            {
                return true;
            }
        }
        if (type1 instanceof TypeVariable<?>) 
        {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type1;
            if (isTypeBounded(type2, EMPTY_TYPES, typeVariable.getBounds())) 
            {
                return true;
            }
        }
        if (type2 instanceof TypeVariable<?>) 
        {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type2;
            if (isTypeBounded(type1, typeVariable.getBounds(), EMPTY_TYPES)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Type type1, Type type2) 
    {
        if (type1 instanceof Class<?>) 
        {
            Class<?> clazz = (Class<?>) type1;
            if (matches(clazz, EMPTY_TYPES, type2)) 
            {
                return true;
            }
        }
        if (type1 instanceof ParameterizedType) 
        {
            ParameterizedType parameterizedType1 = (ParameterizedType) type1;
            if (parameterizedType1.getRawType() instanceof Class<?>) 
            {
                if (matches((Class<?>) parameterizedType1.getRawType(), 
                        parameterizedType1.getActualTypeArguments(), type2)) 
                {
                    return true;
                }
            }
        }
        if (type1 instanceof WildcardType) 
        {
            WildcardType wildcardType = (WildcardType) type1;
            if (isTypeBounded(type2, wildcardType.getLowerBounds(), wildcardType.getUpperBounds())) 
            {
                return true;
            }
        }
        if (type2 instanceof WildcardType) 
        {
            WildcardType wildcardType = (WildcardType) type2;
            if (isTypeBounded(type1, wildcardType.getUpperBounds(), wildcardType.getLowerBounds())) 
            {
                return true;
            }
        }
        if (type1 instanceof TypeVariable<?>) 
        {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type1;
            if (isTypeBounded(type2, EMPTY_TYPES, typeVariable.getBounds())) 
            {
                return true;
            }
        }
        if (type2 instanceof TypeVariable<?>) 
        {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type2;
            if (isTypeBounded(type1, typeVariable.getBounds(), EMPTY_TYPES)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isTypeBounded(Type type, Type[] lowerBounds, Type[] upperBounds) 
    {
        if (lowerBounds.length > 0) 
        {
            if (!isAssignableFrom(type, lowerBounds)) 
            {
                return false;
            }
        }
        if (upperBounds.length > 0) 
        {
            if (!isAssignableFrom(upperBounds, type)) 
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isAssignableFrom(Class<?> rawType1, Type[] actualTypeArguments1, Type type2) 
    {
        if (type2 instanceof ParameterizedType) 
        {
            ParameterizedType parameterizedType = (ParameterizedType) type2;
            if (parameterizedType.getRawType() instanceof Class<?>) 
            {
                if (isAssignableFrom(rawType1, actualTypeArguments1, (Class<?>) parameterizedType.getRawType(), 
                        parameterizedType.getActualTypeArguments())) 
                {
                    return true;
                }
            }
        } 
        else if (type2 instanceof Class<?>) 
        {
            Class<?> clazz = (Class<?>) type2;
            if (isAssignableFrom(rawType1, actualTypeArguments1, clazz, EMPTY_TYPES)) 
            {
                return true;
            }
        } 
        else if (type2 instanceof TypeVariable<?>) 
        {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type2;
            if (isTypeBounded(rawType1, actualTypeArguments1, typeVariable.getBounds())) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Class<?> rawType1, Type[] actualTypeArguments1, Type type2) 
    {
        if (type2 instanceof ParameterizedType) 
        {
            ParameterizedType parameterizedType = (ParameterizedType) type2;
            if (parameterizedType.getRawType() instanceof Class<?>) 
            {
                if (matches(rawType1, actualTypeArguments1, (Class<?>) parameterizedType.getRawType(), 
                        parameterizedType.getActualTypeArguments())) 
                {
                    return true;
                }
            }
        } 
        else if (type2 instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type2;
            if (matches(rawType1, actualTypeArguments1, clazz, EMPTY_TYPES)) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the assiginability of a set of <b>flattened</b> types. This
     * algorithm will check whether any of the types1 matches a type in types2
     *
     * @param types1
     * @param types2
     * @return
     */
    public static boolean isAssignableFrom(Set<Type> types1, Set<Type> types2) 
    {
        for (Type type : types1) 
        {
            if (isAssignableFrom(type, types2)) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether whether any of the types1 matches a type in types2
     *
     * @param types1
     * @param types2
     * @return
     */
    public static boolean matches(Set<Type> types1, Set<Type> types2) 
    {
        for (Type type : types1) 
        {
            if (matches(type, types2)) 
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the assignability of a set of <b>flattened</b> types. This
     * algorithm will check whether any of the types1 matches a type in types2
     *
     * @param types1
     * @param type2
     * @return
     */
    public static boolean isAssignableFrom(Set<Type> types1, Type type2) 
    {
        for (Type type : types1) 
        {
            if (isAssignableFrom(type, type2)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Type[] types1, Type type2) 
    {
        for (Type type : types1) 
        {
            if (isAssignableFrom(type, type2)) 
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isPrimitive(Type type) 
    {
        Class<?> rawType = getRawType(type);
        return rawType == null ? false : rawType.isPrimitive();
    }


}
