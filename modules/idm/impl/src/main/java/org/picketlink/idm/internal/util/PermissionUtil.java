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

import org.picketlink.idm.permission.Permission;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * <p>Utility class for common permission operations.</p>
 *
 * @author Pedro Igor
 */
public class PermissionUtil {

    /**
     * <p>Adds a new <code>operation</code> to <code>operationsCSV</code>, where the latter is a CSV representing the current
     * operationsCSV.</p>
     *
     * @param operationsCSV A CSV of operationsCSV
     * @param operation The operation to add
     *
     * @return The CSV string updated with the given operation.
     */
    public static String addOperation(String operationsCSV, String operation) {
        Set<String> ops = asOperationList(operationsCSV);

        for (String newOperation : asOperationList(operation)) {
            if (!ops.contains(newOperation)) {
                ops.add(newOperation);
            }
        }

        return asOperationCSV(ops);
    }

    /**
     * <p>Removes a new <code>operation</code> to <code>operationsCSV</code>, where the latter is a CSV representing the current
     * operationsCSV.</p>
     *
     * @param operationsCSV A CSV of operationsCSV
     * @param operation The operation to remove
     *
     * @return The CSV string updated without the given operation.
     */
    public static String removeOperation(String operationsCSV, String operation) {
        Set<String> ops = asOperationList(operationsCSV);

        for (String opToRemove : asOperationList(operation)) {
            ops.remove(opToRemove);
        }

        return asOperationCSV(ops);
    }

    /**
     * <p>Returns a {@link java.util.Set} with all operationsCSV from <code>operationsCSV</code>.</p>
     *
     * @param operationsCSV The CSV of operationsCSV.
     *
     * @return A set with all operationsCSV.
     */
    public static Set<String> asOperationList(String operationsCSV) {
        Set<String> operations = new HashSet<String>();

        if (!isNullOrEmpty(operationsCSV)) {
            for (String operation : operationsCSV.split(",")) {
                operations.add(operation.trim());
            }
        }

        return operations;
    }

    /**
     * <p>Check if the given {@link org.picketlink.idm.permission.Permission} has the given attributes.</p>
     *
     * @param permission The permission to check
     * @param resourceClass
     * @param identifier
     * @param operation
     *
     * @return True if the permission has all thi given attributes. Otherwise, false.
     */
    public static boolean hasAttributes(Permission permission, Class<?> resourceClass, Serializable identifier, String operation) {
        if (!permission.getResourceClass().equals(resourceClass)) {
            return false;
        }

        if (!permission.getResourceIdentifier().toString().equals(identifier.toString()) && !identifier.equals(resourceClass.getName())) {
            return false;
        }

        if (hasOperation(permission, operation)) {
            return true;
        }

        return false;
    }

    /**
     * <p>Checks if the fiven given <code>operation</code> is granted by the given {@link org.picketlink.idm.permission.Permission}.</p>
     *
     * @param permission
     * @param operation
     * @return
     */
    public static boolean hasOperation(Permission permission, String operation) {
        String operationToCheck = operation;

        if (operationToCheck == null) {
            operationToCheck = "*";
        }

        for (String op : asOperationList(permission.getOperation())) {
            for (String opCheck : asOperationList(operationToCheck)) {
                if ("*".equals(opCheck.trim()) || op.trim().equals(opCheck.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>Returns a CSV string containing all operations from the give {@link java.util.Set}.</p>
     *
     * @param ops A set with strings where each one represents an operation.
     *
     * @return
     */
    private static String asOperationCSV(Set<String> ops) {
        StringBuilder sb = new StringBuilder();

        for (String op : ops) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(op);
        }

        return sb.toString();
    }
}
