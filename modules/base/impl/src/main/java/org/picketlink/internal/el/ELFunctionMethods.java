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
package org.picketlink.internal.el;

import org.picketlink.Identity;
import org.picketlink.authorization.util.AuthorizationUtil;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Account;

/**
 * <p>Provides some built-in EL functions.</p>
 *
 * @author Pedro Igor
 */
public class ELFunctionMethods {

    /**
     * <p>Checks if the user is logged in.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @return True if the user is logged in. Otherwise, returns false.
     */
    public static boolean isLoggedIn() {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();

        return AuthorizationUtil.isLoggedIn(identity);
    }

    /**
     * <p>Checks if the user has permissions to a resource considering an operation.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @param resource The resource.
     * @param operation The operation.
     *
     * @return True if the user has permission. Otherwise, returns false.
     */
    public static boolean hasPermission(Object resource, String operation) {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();

        return AuthorizationUtil.hasPermission(identity, resource, null, null, operation);
    }

    /**
     * <p>Checks if an authenticated user is granted with a role with the given name.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @param roleName The role name.
     *
     * @return True if the user is granted with the role. Otherwise, returns false.
     */
    public static boolean hasRole(String roleName) {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();
        PartitionManager partitionManager = evaluationContext.getPartitionManager();

        return AuthorizationUtil.hasRole(identity, partitionManager, roleName);
    }

    /**
     * <p>Checks if an authenticated user is member of the a group with the given name.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @param groupName The group name.
     *
     * @return True if the user is a member of the group. Otherwise, returns false.
     */
    public static boolean isMember(String groupName) {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();
        PartitionManager partitionManager = evaluationContext.getPartitionManager();

        return AuthorizationUtil.isMember(identity, partitionManager, groupName);
    }

    /**
     * <p>Checks if an authenticated user is setted with an attribute with the given name.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @param attributeName The attribute's name.
     *
     * @return True if the user is setted with the attribute. Otherwise, returns false.
     */
    public static boolean hasAttribute(String attributeName) {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();
        Account account = identity.getAccount();

        if (account != null && account.getAttribute(attributeName) != null) {
            return true;
        }

        return false;
    }

    /**
     * <p>Checks if an authenticated user is associated with a partition with the given name.</p>
     *
     * <p>This method requires that valid {@link ELEvaluationContext} associated with the current
     * invation thread.</p>
     *
     * @param partitionName The partition name.
     *
     * @return True if the user is associated with the partition. Otherwise, returns false.
     */
    public static boolean hasPartition(String partitionName) {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();
        Identity identity = evaluationContext.getIdentity();

        return AuthorizationUtil.hasPartition(identity, null, new String[]{partitionName});
    }
}