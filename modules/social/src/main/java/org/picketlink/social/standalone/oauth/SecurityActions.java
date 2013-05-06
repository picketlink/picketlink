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
package org.picketlink.social.standalone.oauth;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged Blocks
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
class SecurityActions {
    static Class<?> loadClass(final Class<?> theClass, final String fqn) {
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            public Class<?> run() {
                ClassLoader classLoader = theClass.getClassLoader();

                Class<?> clazz = loadClass(classLoader, fqn);
                if (clazz == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                    clazz = loadClass(classLoader, fqn);
                }
                return clazz;
            }
        });
    }

    static Class<?> loadClass(final ClassLoader cl, final String fqn) {
        return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
            public Class<?> run() {
                try {
                    return cl.loadClass(fqn);
                } catch (ClassNotFoundException e) {
                }
                return null;
            }
        });
    }

    /**
     * Set the system property
     *
     * @param key
     * @param defaultValue
     * @return
     */
    static void setSystemProperty(final String key, final String value) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                System.setProperty(key, value);
                return null;
            }
        });
    }

    /**
     * Get the system property
     *
     * @param key
     * @param defaultValue
     * @return
     */
    static String getSystemProperty(final String key, final String defaultValue) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, defaultValue);
            }
        });
    }

    /**
     * Load a resource based on the passed {@link Class} classloader. Failing which try with the Thread Context CL
     *
     * @param clazz
     * @param resourceName
     * @return
     */
    static URL loadResource(final Class<?> clazz, final String resourceName) {
        return AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run() {
                URL url = null;
                ClassLoader clazzLoader = clazz.getClassLoader();
                url = clazzLoader.getResource(resourceName);

                if (url == null) {
                    clazzLoader = Thread.currentThread().getContextClassLoader();
                    url = clazzLoader.getResource(resourceName);
                }

                return url;
            }
        });
    }
}