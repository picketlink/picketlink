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
package org.picketlink.idm.internal.util;

/**
 * General purpose Util
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 *
 */
public class IDMUtil {

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

}