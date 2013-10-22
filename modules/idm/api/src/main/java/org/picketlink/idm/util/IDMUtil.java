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
package org.picketlink.idm.util;

import org.picketlink.idm.model.AttributedType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * General purpose Util
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class IDMUtil {

    /**
     * <p>Converts the given array into a {@link Set}.</p>
     *
     * @param values
     * @param <P>
     *
     * @return
     */
    public static <P> Set<P> toSet(P[] values) {
        return new HashSet<P>(Arrays.asList(values));
    }

    /**
     * <p>This method checks if a <code>type</code> is supported by looking at the <code>supportedTypes</code> and
     * the <code>unsupportedTypes</code> set.</p>
     *
     * <p>The calculation is based on the type equality and also its hierarchy.</p>
     *
     * @param type The target supported type.
     * @param supportedTypes A Set with all supported types.
     * @param unsupportedTypes A Set with all unsupported types.
     *
     * @return -1 if the type is not supported. 0 if the type exactly matches. If  > 0 it is supported considering the
     *         hierarchy.
     */
    public static <P extends Class<?>> int isTypeSupported(P type, Set<P> supportedTypes, Set<P> unsupportedTypes) {
        int score = -1;

        for (P cls : supportedTypes) {
            int clsScore = calcScore(type, cls);
            if (clsScore > score && supportedTypes.contains(cls)) {
                score = clsScore;
            }
        }

        for (Class<?> cls : unsupportedTypes) {
            if (cls.isAssignableFrom(type) && unsupportedTypes.contains(cls)) {
                score = -1;
                break;
            }
        }

        return score;
    }

    public static int isTypeOperationSupported(Class<? extends AttributedType> type,
                                               IdentityOperation operation,
                                               Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
                                               Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes) {
        int score = -1;

        for (Class<? extends AttributedType> cls : supportedTypes.keySet()) {
            int clsScore = calcScore(type, cls);
            if (clsScore > score && supportedTypes.get(cls).contains(operation)) {
                score = clsScore;
            }
        }

        for (Class<? extends AttributedType> cls : unsupportedTypes.keySet()) {
            if (cls.isAssignableFrom(type) && unsupportedTypes.get(cls).contains(operation)) {
                score = -1;
                break;
            }
        }

        return score;
    }


    private static int calcScore(Class<?> type, Class<?> targetClass) {
        if (type.equals(targetClass)) {
            return 0;
        } else if (targetClass.isAssignableFrom(type)) {
            int score = 0;

            Class<?> cls = type.getSuperclass();
            while (!cls.equals(Object.class)) {
                if (targetClass.isAssignableFrom(cls)) {
                    score++;
                } else {
                    break;
                }
                cls = cls.getSuperclass();
            }
            return score;
        } else if (type.isAssignableFrom(targetClass)) {
            // in this case, we check if the type is a parent, so we can calc the hierarchy
            int score = 0;

            Class<?> cls = targetClass.getSuperclass();
            while (!cls.equals(Object.class)) {
                if (type.isAssignableFrom(cls)) {
                    score++;
                } else {
                    break;
                }
                cls = cls.getSuperclass();
            }
            return score;
        } else {
            return -1;
        }
    }
}