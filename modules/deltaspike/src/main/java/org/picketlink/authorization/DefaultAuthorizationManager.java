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
import org.picketlink.authentication.levels.InsufficientSecurityLevelException;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authorization.annotations.GroupsAllowed;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.PartitionsAllowed;
import org.picketlink.authorization.annotations.RequiresLevel;
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
import org.picketlink.producer.LevelFactoryResolver;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.apache.deltaspike.core.util.ProxyUtils.getUnproxiedClass;

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

    @Inject
    private LevelFactoryResolver abstractFactory;

    @Secures
    @LoggedIn
    public boolean isLoggedIn(InvocationContext invocationContext) {
        LoggedIn loggedIn = getAnnotation(invocationContext, LoggedIn.class);
        Class<? extends Account> accountType = Account.class;

        if (loggedIn != null) {
            accountType = loggedIn.requiresAccount();
        }

        Account account = getIdentity().getAccount();

        return account != null && accountType.isInstance(account);
    }

    @Secures
    @Restrict
    public boolean checkExpression(InvocationContext invocationContext) {
        Restrict restrict = getAnnotation(invocationContext, Restrict.class);
        String expression = restrict.value();
        Object result = this.elProcessor.eval(expression);

        if (Boolean.class.isInstance(result)) {
            return Boolean.valueOf(result.toString());
        }

        return false;
    }

    @Secures
    @RequiresPermission
    public boolean hasPermission(InvocationContext invocationContext) {
        RequiresPermission requiresPermission = getAnnotation(invocationContext, RequiresPermission.class);
        String resource = requiresPermission.resource();
        Class<?> resourceClass = requiresPermission.resourceClass();
        String resourceIdentifier = requiresPermission.resourceIdentifier();
        String operation = requiresPermission.operation();

        return AuthorizationUtil.hasPermission(getIdentity(), resource, resourceClass, resourceIdentifier, operation);
    }

    @Secures
    @RolesAllowed
    public boolean hasRole(InvocationContext invocationContext) {
        RolesAllowed rolesAllowed = getAnnotation(invocationContext, RolesAllowed.class);

        for (String roleName : rolesAllowed.value()) {
            if (AuthorizationUtil.hasRole(getIdentity(), this.partitionManager, roleName)) {
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
            if (AuthorizationUtil.isMember(getIdentity(), this.partitionManager, groupName)) {
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

    @Secures
    @RequiresLevel
    public boolean hasLevel(InvocationContext invocationContext){
        RequiresLevel requireslevel = getAnnotation(invocationContext,RequiresLevel.class);
        String level = requireslevel.value();

        Level requiredLevel = abstractFactory.resolve().createLevel(level);
        if (!AuthorizationUtil.hasLevel(identity, requiredLevel)){
            throw new InsufficientSecurityLevelException(requiredLevel,
                    "Expected security level is: " + requiredLevel + " but the current level is: " +identity.getLevel());
        }

        return true;
    }

    private <T extends Annotation> T getAnnotation(InvocationContext invocationContext, Class<T> annotationType) {
        Class unproxiedClass = getUnproxiedClass(invocationContext.getTarget().getClass());
        T annotation = (T) unproxiedClass.getAnnotation(annotationType);
        Method invocationContextMethod = invocationContext.getMethod();

        if (annotation == null) {
            annotation = invocationContextMethod.getAnnotation(annotationType);
        }

        if (annotation == null) {
            throw new IllegalArgumentException("No annotation [" + annotationType + "] found in type [" + unproxiedClass + "] or method [" + invocationContextMethod + ".");
        }

        return annotation;
    }

    private Identity getIdentity() {
        return this.identity;
    }
}