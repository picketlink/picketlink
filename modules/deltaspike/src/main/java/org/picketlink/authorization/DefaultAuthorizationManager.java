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
package org.picketlink.authorization;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.picketlink.Identity;
import org.picketlink.authorization.annotations.GroupsAllowed;
import org.picketlink.authorization.annotations.PartitionsAllowed;
import org.picketlink.authorization.annotations.RequiresAccount;
import org.picketlink.authorization.annotations.RequiresPermission;
import org.picketlink.authorization.annotations.Restrict;
import org.picketlink.authorization.annotations.RolesAllowed;
import org.picketlink.authorization.util.AuthorizationUtil;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Partition;
import org.picketlink.internal.el.ELProcessor;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

/**
 * <p>Default implementation of the authorization checks provided by the built-in security annotations provided by PicketLink.</p>
 *
 * @author Pedro Igor
 */
public class DefaultAuthorizationManager {

    @Inject
    private BeanManager beanManager;

    @Inject
    @Any
    private Identity identity;

    @Inject
    private ELProcessor elProcessor;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Secures
    @RequiresAccount
    public boolean isLoggedIn(InvocationContext invocationContext) {
        RequiresAccount requiresAccount = getAnnotation(invocationContext, RequiresAccount.class);
        Class<? extends Account> accountType = Account.class;

        if (requiresAccount != null) {
            accountType = requiresAccount.type();
        }

        Account account = getIdentity().getAccount();

        return account != null && accountType.isInstance(account);
    }

    @Secures
    @Restrict
    public boolean checkExpression(InvocationContext invocationContext) {
        if (isLoggedIn(invocationContext)) {
            Restrict restrict = getAnnotation(invocationContext, Restrict.class);
            String expression = restrict.value();
            Object result = this.elProcessor.eval(expression);

            if (Boolean.class.isInstance(result)) {
                return Boolean.valueOf(result.toString());
            }
        }

        return false;
    }

    @Secures
    @RequiresPermission
    public boolean hasPermission(InvocationContext invocationContext) {
        RequiresPermission requiresPermission = getAnnotation(invocationContext, RequiresPermission.class);
        String resource = requiresPermission.resource();
        String operation = requiresPermission.operation();

        if (AuthorizationUtil.hasPermission(getIdentity(), resource, operation)) {
            return true;
        }

        return false;
    }

    @Secures
    @RolesAllowed
    public boolean hasRole(InvocationContext invocationContext) {
        RolesAllowed rolesAllowed = getAnnotation(invocationContext, RolesAllowed.class);

        for (String roleName : rolesAllowed.value()) {
            if (AuthorizationUtil.hasRole(getIdentity(), this.partitionManager, this.identityManager, this.relationshipManager, roleName)) {
                return true;
            }
        }

        return false;
    }

    @Secures
    @GroupsAllowed
    public boolean isMember(InvocationContext invocationContext) {
        GroupsAllowed groupsAllowed = getAnnotation(invocationContext, GroupsAllowed.class);
        String[] groupNames = groupsAllowed.value();

        for (String groupName : groupNames) {
            if (AuthorizationUtil.isMember(getIdentity(), this.partitionManager, this.identityManager, this.relationshipManager, groupName)) {
                return true;
            }
        }

        return false;
    }

    @Secures
    @PartitionsAllowed
    public boolean hasPartition(InvocationContext invocationContext) {
        PartitionsAllowed partitionsAllowed = getAnnotation(invocationContext, PartitionsAllowed.class);
        String[] partitionNames = partitionsAllowed.name();
        Class<? extends Partition> partitionType = partitionsAllowed.type();

        return AuthorizationUtil.hasPartition(getIdentity(), partitionType, partitionNames);
    }

    private <T extends Annotation> T getAnnotation(InvocationContext invocationContext, Class<T> annotationType) {
        Object targetBean = invocationContext.getTarget();
        T annotation = targetBean.getClass().getAnnotation(annotationType);

        if (annotation == null) {
            annotation = invocationContext.getMethod().getAnnotation(annotationType);
        }

        return annotation;
    }

    private Identity getIdentity() {
        return this.identity;
    }
}