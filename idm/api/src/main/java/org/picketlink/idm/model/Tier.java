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

package org.picketlink.idm.model;

/**
 * A hierarchical abstraction representing a partitioned set or subset of services, for which
 * specialized Roles and Groups may be created.
 *
 * @author Shane Bryzak
 */
public class Tier extends AbstractPartition {

    private static final long serialVersionUID = 7797059334915537276L;

    private Tier parent;

    public Tier(String id) {
        super(id);
    }

}
