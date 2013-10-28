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
package org.picketlink.idm.jdbc.internal.mappers;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.jdbc.internal.model.AbstractJdbcType;
import org.picketlink.idm.jdbc.internal.model.IdentityManagedJdbcType;
import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.jdbc.internal.model.RelationshipJdbcType;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

/**
 * Class that is able to map entity types
 * @author Anil Saldhana
 * @since October 23, 2013
 */
public class JdbcMapper {
    private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

    public JdbcMapper() {
        classMap.put(Agent.class.getName(), IdentityManagedJdbcType.class);
        classMap.put(IdentityType.class.getName(), IdentityManagedJdbcType.class);
        classMap.put(User.class.getName(), IdentityManagedJdbcType.class);
        classMap.put(Role.class.getName(), IdentityManagedJdbcType.class);
        classMap.put(Group.class.getName(), IdentityManagedJdbcType.class);
        classMap.put(Grant.class.getName(), RelationshipJdbcType.class);
        classMap.put(GroupMembership.class.getName(), RelationshipJdbcType.class);
        classMap.put(Partition.class.getName(), PartitionJdbcType.class);
        classMap.put(PartitionJdbcType.class.getName(), PartitionJdbcType.class);
    }

    public <T extends AbstractJdbcType> T getInstance(Class<? extends AttributedType> clazz) {
        Class<?> storedClass = classMap.get(clazz.getName());
        if (storedClass != null) {
            try {
                return (T) storedClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Unable to find instance for " + clazz.getName());
    }

    /**
     * Update the mapping for a class
     * @param key the name of the class such as org.picketlink.idm.model.basic.Agent
     * @param value {@link Class} of the mapping class
     */
    public static void map(String key, Class<?> value){
        classMap.put(key,value);
    }
}