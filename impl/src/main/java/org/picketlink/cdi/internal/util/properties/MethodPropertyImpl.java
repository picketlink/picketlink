package org.picketlink.cdi.internal.util.properties;

import java.beans.Introspector;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.picketlink.cdi.internal.util.reflection.Reflections;
import static org.picketlink.cdi.internal.util.reflection.Reflections.invokeMethod;

/**
 * A bean property based on the value represented by a getter/setter method pair
 *
 */
class MethodPropertyImpl<V> implements MethodProperty<V> 
{
    private static final String GETTER_METHOD_PREFIX = "get";
    private static final String SETTER_METHOD_PREFIX = "set";
    private static final String BOOLEAN_GETTER_METHOD_PREFIX = "is";

    private static final int GETTER_METHOD_PREFIX_LENGTH = GETTER_METHOD_PREFIX.length();
    private static final int SETTER_METHOD_PREFIX_LENGTH = SETTER_METHOD_PREFIX.length();
    private static final int BOOLEAN_GETTER_METHOD_PREFIX_LENGTH = BOOLEAN_GETTER_METHOD_PREFIX.length();

    private final Method getterMethod;
    private final String propertyName;
    private final Method setterMethod;

    public MethodPropertyImpl(Method method) 
    {
        final String accessorMethodPrefix;
        final String propertyNameInAccessorMethod;
        
        if (method.getName().startsWith(GETTER_METHOD_PREFIX)) 
        {
            if (method.getReturnType() == Void.TYPE) 
            {
                throw new IllegalArgumentException(
                        "Invalid accessor method, must have return value if starts with 'get'. Method: " + method);
            } 
            else if (method.getParameterTypes().length > 0) 
            {
                throw new IllegalArgumentException(
                        "Invalid accessor method, must have zero arguments if starts with 'get'. Method: " + method);
            }
            propertyNameInAccessorMethod = method.getName().substring(GETTER_METHOD_PREFIX_LENGTH);
            accessorMethodPrefix = GETTER_METHOD_PREFIX;
        } 
        else if (method.getName().startsWith(SETTER_METHOD_PREFIX)) 
        {
            if (method.getReturnType() != Void.TYPE) 
            {
                throw new IllegalArgumentException(
                        "Invalid accessor method, must not have return value if starts with 'set'. Method: " + method);
            } 
            else if (method.getParameterTypes().length != 1) 
            {
                throw new IllegalArgumentException(
                        "Invalid accessor method, must have one argument if starts with 'set'. Method: " + method);
            }
            propertyNameInAccessorMethod = method.getName().substring(SETTER_METHOD_PREFIX_LENGTH);
            accessorMethodPrefix = SETTER_METHOD_PREFIX;
        } 
        else if (method.getName().startsWith(BOOLEAN_GETTER_METHOD_PREFIX)) 
        {
            if (method.getReturnType() != Boolean.TYPE || !method.getReturnType().isPrimitive()) 
            {
                throw new IllegalArgumentException(
                        "Invalid accessor method, must return boolean primitive if starts with 'is'. Method: " + 
                         method);
            }
            propertyNameInAccessorMethod = method.getName().substring(BOOLEAN_GETTER_METHOD_PREFIX_LENGTH);
            accessorMethodPrefix = BOOLEAN_GETTER_METHOD_PREFIX;
        } 
        else 
        {
            throw new IllegalArgumentException(
                    "Invalid accessor method, must start with 'get', 'set' or 'is'.  " + "Method: " + method);
        }
        
        if (propertyNameInAccessorMethod.length() == 0 || 
                !Character.isUpperCase(propertyNameInAccessorMethod.charAt(0))) 
        {
            throw new IllegalArgumentException("Invalid accessor method, prefix '" + accessorMethodPrefix +
                    "' must be followed a non-empty property name, capitalized. Method: " + method);
        }
        
        this.propertyName = Introspector.decapitalize(propertyNameInAccessorMethod);
        this.getterMethod = getGetterMethod(method.getDeclaringClass(), propertyName);
        this.setterMethod = getSetterMethod(method.getDeclaringClass(), propertyName);
    }

    public String getName() 
    {
        return propertyName;
    }

    @SuppressWarnings("unchecked")
    public Class<V> getJavaClass() 
    {
        return (Class<V>) getterMethod.getReturnType();
    }

    public Type getBaseType() 
    {
        return getterMethod.getGenericReturnType();
    }

    public Method getAnnotatedElement() 
    {
        return getterMethod;
    }

    public Member getMember() 
    {
        return getterMethod;
    }

    public V getValue(Object instance) 
    {
        if (getterMethod == null) 
        {
            throw new UnsupportedOperationException("Property " + 
                this.setterMethod.getDeclaringClass() + "." + propertyName + 
                " cannot be read, as there is no getter method.");
        }
        return Reflections.cast(invokeMethod(getterMethod, instance));
    }

    public void setValue(Object instance, V value) 
    {
        if (setterMethod == null) 
        {
            throw new UnsupportedOperationException("Property " + 
                this.getterMethod.getDeclaringClass() + "." + propertyName + 
                " is read only, as there is no setter method.");
        }
        invokeMethod(setterMethod, instance, value);
    }

    private static Method getSetterMethod(Class<?> clazz, String name) 
    {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) 
        {
            String methodName = method.getName();
            if (methodName.startsWith(SETTER_METHOD_PREFIX) && method.getParameterTypes().length == 1) 
            {
                if (Introspector.decapitalize(methodName.substring(SETTER_METHOD_PREFIX_LENGTH)).equals(name)) 
                {
                    return method;
                }
            }
        }
        return null;
    }

    private static Method getGetterMethod(Class<?> clazz, String name) 
    {
        for (Method method : clazz.getDeclaredMethods()) 
        {
            String methodName = method.getName();
            if (method.getParameterTypes().length == 0) 
            {
                if (methodName.startsWith(GETTER_METHOD_PREFIX)) 
                {
                    if (Introspector.decapitalize(methodName.substring(GETTER_METHOD_PREFIX_LENGTH)).equals(name)) 
                    {
                        return method;
                    }
                } 
                else if (methodName.startsWith(BOOLEAN_GETTER_METHOD_PREFIX)) 
                {
                    if (Introspector.decapitalize(
                            methodName.substring(BOOLEAN_GETTER_METHOD_PREFIX_LENGTH)).equals(name)) 
                    {
                        return method;
                    }
                }
            }
        }
        throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
    }

    public Class<?> getDeclaringClass() 
    {
        return getterMethod.getDeclaringClass();
    }

    public boolean isReadOnly() 
    {
        return setterMethod == null;
    }

    public void setAccessible() 
    {
        if (setterMethod != null) 
        {
            Reflections.setAccessible(setterMethod);
        }
        if (getterMethod != null) 
        {
            Reflections.setAccessible(getterMethod);
        }
    }

    @Override
    public String toString() 
    {
        StringBuilder builder = new StringBuilder();
        if (isReadOnly()) 
        {
            builder.append("read-only ").append(setterMethod.toString()).append("; ");
        }
        builder.append(getterMethod.toString());
        return builder.toString();
    }

    @Override
    public int hashCode() 
    {
        int hash = 1;
        hash = hash * 31 + (setterMethod == null ? 0 : setterMethod.hashCode());
        hash = hash * 31 + getterMethod.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj instanceof MethodPropertyImpl<?>) 
        {
            MethodPropertyImpl<?> that = (MethodPropertyImpl<?>) obj;
            if (this.setterMethod == null) 
            {
                return that.setterMethod == null && this.getterMethod.equals(that.getterMethod);
            } 
            else 
            {
                return this.setterMethod.equals(that.setterMethod) && this.getterMethod.equals(that.getterMethod);
            }
        } 
        else 
        {
            return false;
        }
    }

}
