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

package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Pedro Silva
 *
 */
public class IdentityTypeHandlerFactory {

    /**
     * <p>
     * Defines a map with all {@link IdentityTypeHandler} with the specific logic to handle the different {@link IdentityType}
     * types.
     * </p>
     */
    private static Map<Class<? extends IdentityType>, IdentityTypeHandler<? extends IdentityType>> identityTypeHandlers = new HashMap<Class<? extends IdentityType>, IdentityTypeHandler<? extends IdentityType>>();

    static {
        identityTypeHandlers.put(User.class, new UserHandler());
        identityTypeHandlers.put(Agent.class, new AgentHandler());
        identityTypeHandlers.put(Role.class, new RoleHandler());
        identityTypeHandlers.put(Group.class, new GroupHandler());
    }

    @SuppressWarnings("unchecked")
    public static IdentityTypeHandler<IdentityType> getHandler(Class<? extends IdentityType> identityTypeClass) {
        for (Entry<Class<? extends IdentityType>, IdentityTypeHandler<? extends IdentityType>> entry : identityTypeHandlers.entrySet()) {
            Class<? extends IdentityType> handlerClass = entry.getKey();

            if (handlerClass.isAssignableFrom(identityTypeClass)) {
                if (Agent.class.equals(handlerClass) && User.class.isAssignableFrom(identityTypeClass)) {
                    continue;
                }

                return (IdentityTypeHandler<IdentityType>) entry.getValue();
            }
        }

        throw new IdentityManagementException("No handler found for IdentityType [" + identityTypeClass.getName() + "].");
    }

}
