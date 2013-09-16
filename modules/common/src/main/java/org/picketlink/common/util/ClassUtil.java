package org.picketlink.common.util;

/**
 * <p>Utility dealing with {@link Class}.</p>
 *
 * @author Pedro Igor
 */
public final class ClassUtil {

    /**
     * <p>Loads a class with the given <code>fullQualifiedName</code>.</p>
     * <p>This method tries first to load from the specified <code>fromClass</code>, if not found it will try to load from using TCCL.</p>
     *
     * @param fromClass
     * @param fullQualifiedName
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
            throw new RuntimeException("Could not load class [" + fullQualifiedName + "].");
        }

        return attributedTypeClass;
    }

}
