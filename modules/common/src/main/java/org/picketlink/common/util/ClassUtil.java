package org.picketlink.common.util;

import org.picketlink.common.exceptions.PicketLinkException;

/**
 * <p>Utility dealing with {@link Class}.</p>
 *
 * @author Pedro Igor
 */
public final class ClassUtil {

    /**
     * <p>Loads a class with the given <code>fullQualifiedName</code>.</p>
     *
     * @param fromClass
     * @param fullQualifiedName
     *
     * @return
     */
    public static Class<?> loadClass(Class<?> fromClass, final String fullQualifiedName) {
        Class<?> attributedTypeClass = null;

        try {
            attributedTypeClass = (Class<?>) fromClass.getClassLoader().loadClass(fullQualifiedName);
        } catch (ClassNotFoundException cnfe) {
            try {
                attributedTypeClass = (Class<?>) Thread.currentThread().getContextClassLoader().loadClass(fullQualifiedName);
            } catch (ClassNotFoundException e) {
            }
        }

        if (attributedTypeClass == null) {
            throw new PicketLinkException("Could not load class [" + fullQualifiedName + "].");
        }

        return attributedTypeClass;
    }

    /**
     * <p></p>
     *
     * @param fromClass The class that will be used to get the classloader to load the instance class.
     * @param fullQualifiedName The full qualified name of the class from which the instance will be created.
     *
     * @return An instance of the class specified by the <code>fullQualifiedName</code> argument. This method tries
     *         first to load from the specified <code>fromClass</code> class loader, if not found it will try to load
     *         from using TCCL.
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object newInstance(Class<?> fromClass, final String fullQualifiedName) throws IllegalAccessException, InstantiationException {
        return loadClass(fromClass, fullQualifiedName).newInstance();
    }

    public static <T> T newInstance(final Class<T> fromClass) throws IllegalAccessException, InstantiationException {
        return (T) loadClass(fromClass, fromClass.getName()).newInstance();
    }
}