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
package org.picketlink.social.standalone.login;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged Blocks
 *
 * @author Anil Saldhana
 * @since Sep 26, 2011
 */
class SecurityActions {
    /**
     * Use reflection to get the {@link Method} on a {@link Class} with the given parameter types
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
                try {
                    return clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (Exception e) {
                    return null;
                }
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
        if(System.getSecurityManager() == null){
            return System.getProperty(key, defaultValue);
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, defaultValue);
            }
        });
    }
}