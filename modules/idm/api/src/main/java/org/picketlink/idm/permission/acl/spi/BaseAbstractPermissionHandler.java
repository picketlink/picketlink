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

package org.picketlink.idm.permission.acl.spi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.permission.annotations.AllowedOperation;
import org.picketlink.idm.permission.annotations.AllowedOperations;

/**
 * Stored resource permissions can either be persisted as a comma-separated list of values, or as a
 * bit-masked numerical value where each bit represents a specific permission for that class. This
 * is a helper class that handles the conversion automatically and presents a unified API for
 * dealing with these persistent actions.
 *
 * @author Shane Bryzak
 */
public abstract class BaseAbstractPermissionHandler implements PermissionHandler {
    private Map<Class<?>, Boolean> usesMask = new HashMap<Class<?>, Boolean>();

    private Map<Class<?>, Map<String, Long>> instanceOperations = new ConcurrentHashMap<Class<?>, Map<String, Long>>();

    private Map<Class<?>, Map<String, Long>> classOperations = new ConcurrentHashMap<Class<?>, Map<String, Long>>();

    private synchronized void initOperations(Class<?> cls) {
        if (!instanceOperations.containsKey(cls)) {

            Map<String, Long> instanceOps = new HashMap<String, Long>();
            Map<String, Long> classOps = new HashMap<String, Long>();

            boolean useMask = false;

            AllowedOperations p = (AllowedOperations) cls.getAnnotation(AllowedOperations.class);

            if (p != null) {
                AllowedOperation[] operations = p.value();
                if (operations != null) {
                    for (AllowedOperation operation : operations) {

                        if (operation.classOperation()) {
                            classOps.put(operation.value(), operation.mask());
                        } else {
                            instanceOps.put(operation.value(), operation.mask());
                        }

                        if (operation.mask() != 0) {
                            useMask = true;
                        }
                    }
                }
            }

            // Validate that all actions have a proper mask
            if (useMask) {
                Set<Long> masks = new HashSet<Long>();

                Set<String> ops = new HashSet<String>();
                ops.addAll(instanceOps.keySet());
                ops.addAll(classOps.keySet());

                for (String op : ops) {

                    Long mask = instanceOps.containsKey(op) ? instanceOps.get(op) : classOps.get(op);
                    if (masks.contains(mask)) {
                        throw new IllegalArgumentException("Class " + cls.getName() +
                                " defines a duplicate mask for permission operation [" + op + "]");
                    }

                    if (mask == 0) {
                        throw new IllegalArgumentException("Class " + cls.getName() +
                                " must define a valid mask value for operation [" + op + "]");
                    }

                    if ((mask & (mask - 1)) != 0) {
                        throw new IllegalArgumentException("Class " + cls.getName() +
                                " must define a mask value that is a power of 2 for operation [" + op + "]");
                    }

                    masks.add(mask);
                }
            }

            usesMask.put(cls, useMask);
            instanceOperations.put(cls, instanceOps);
        }
    }

    protected class PermissionSet {
        private Set<String> permissions = new HashSet<String>();

        private Class<?> resourceClass;

        public PermissionSet(Class<?> resourceClass, String members) {
            this.resourceClass = resourceClass;
            addMembers(members);
        }

        public void addMembers(String members) {
            if (members == null) {
                return;
            }

            if (usesMask.get(resourceClass)) {
                // bit mask-based actions
                long vals = Long.valueOf(members);

                Map<String, Long> permissions = instanceOperations.get(resourceClass);
                for (String permission : permissions.keySet()) {
                    long mask = permissions.get(permission).longValue();
                    if ((vals & mask) != 0) {
                        this.permissions.add(permission);
                    }
                }
            }
            else {
                // comma-separated string based actions
                String[] permissions = members.split(",");
                for (String permission : permissions) {
                    this.permissions.add(permission);
                }
            }
        }

        public boolean contains(String action) {
            return permissions.contains(action);
        }

        public PermissionSet add(String action) {
            permissions.add(action);
            return this;
        }

        public PermissionSet remove(String action) {
            permissions.remove(action);
            return this;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public boolean isEmpty() {
            return permissions.isEmpty();
        }

        @Override
        public String toString() {
            if (usesMask.get(resourceClass)) {
                Map<String, Long> actions = instanceOperations.get(resourceClass);
                long mask = 0;

                for (String member : permissions) {
                    mask |= actions.get(member).longValue();
                }

                return "" + mask;
            } else {
                StringBuilder sb = new StringBuilder();
                for (String member : permissions) {
                    if (sb.length() > 0) {
                        sb.append(',');
                    }
                    sb.append(member);
                }
                return sb.toString();
            }
        }
    }

    public PermissionSet createPermissionSet(Class<?> resourceClass, String members) {
        if (!instanceOperations.containsKey(resourceClass)) {
            initOperations(resourceClass);
        }

        return new PermissionSet(resourceClass, members);
    }

    @Override
    public Set<String> listClassOperations(Class<?> resourceClass) {
        if (!classOperations.containsKey(resourceClass)) {
            initOperations(resourceClass);
        }

        Set<String> permissions = new HashSet<String>();

        for (String permission : classOperations.get(resourceClass).keySet()) {
            permissions.add(permission);
        }

        return permissions;
    }

    @Override
    public Set<String> listInstanceOperations(Class<?> resourceClass) {
        if (!instanceOperations.containsKey(resourceClass)) {
            initOperations(resourceClass);
        }

        Set<String> permissions = new HashSet<String>();

        for (String permission : instanceOperations.get(resourceClass).keySet()) {
            permissions.add(permission);
        }

        return permissions;
    }
}
