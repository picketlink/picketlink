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
package org.picketlink.idm.internal.util;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.QueryParameter;

/**
 * General purpose Util
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 *
 */
public class IDMUtil {

    public static boolean isGroupType(Class<? extends IdentityType> identityType) {
        return org.picketlink.idm.model.sample.Group.class.isAssignableFrom(identityType);
    }

    public static boolean isRoleType(Class<? extends IdentityType> identityType) {
        return org.picketlink.idm.model.sample.Role.class.isAssignableFrom(identityType);
    }

    public static boolean isUserType(Class<? extends IdentityType> identityType) {
        return org.picketlink.idm.model.sample.User.class.isAssignableFrom(identityType);
    }

    public static boolean isAgentType(Class<? extends IdentityType> identityType) {
        return org.picketlink.idm.model.sample.Agent.class.isAssignableFrom(identityType);
    }

    /**
     * Return default criterias for sorting query results. Those are used by default if there are not sorting criterias
     * specified by user
     *
     * @param clazz identity type
     * @return default sorting criteria for particular identity type
     */
    public static QueryParameter[] getDefaultParamsForSorting(Class<? extends IdentityType> clazz) {
        if (isAgentType(clazz)) {
            return new QueryParameter[] { org.picketlink.idm.model.sample.Agent.LOGIN_NAME };
        } else if (isGroupType(clazz)) {
            return new QueryParameter[] { org.picketlink.idm.model.sample.Group.NAME };
        } else if (isRoleType(clazz)) {
            return new QueryParameter[] { org.picketlink.idm.model.sample.Role.NAME };
        } else {
            return new QueryParameter[] { IdentityType.ID };
        }
    }

    /**
     * Match two arrays for equality
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static boolean arraysEqual(String[] arr1, String[] arr2) {
        if (arr1 != null && arr2 == null) {
            return false;
        }
        if (arr1 == null && arr2 == null) {
            return true;
        }
        if (arr1 == null && arr2 != null) {
            return false;
        }

        int length1 = arr1.length;
        int length2 = arr2.length;
        if (length1 != length2) {
            return false;
        }
        boolean foundMatch = false;
        for (int i = 0; i < length1; i++) {
            for (int j = 0; j < length2; j++) {
                if (arr1[i].equals(arr2[j])) {
                    foundMatch = true;
                    break;
                }
            }
            if (foundMatch == false) {
                return false;
            }
            // reset
            foundMatch = false;
        }
        return true;
    }

    /**
     * Sleep given number of milliseconds
     *
     * @param ms
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}