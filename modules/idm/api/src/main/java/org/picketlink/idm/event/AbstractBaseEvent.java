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

package org.picketlink.idm.event;

import org.picketlink.idm.PartitionManager;

/**
 * <p>A base class for all event class that provides an event context.</p>
 *
 * <p>Events provide access to the {@link org.picketlink.idm.PartitionManager} from where the event was fired directly or indirectly
 * by its corrsponding {@link org.picketlink.idm.IdentityManager} or {@link org.picketlink.idm.RelationshipManager} instances.
 * The reason for that is that events may be fired during initialization where the environment has not finished the process that
 * makes the partition manager available for the application. For instance, when using CDI the partition manager may be
 * initialized and a new partition created before instances are produced.</p>
 *
 * @author Shane Bryzak
 */
public abstract class AbstractBaseEvent {

    private final EventContext context = new EventContext();
    private final PartitionManager partitionMananger;

    public AbstractBaseEvent(PartitionManager partitionManager) {
        this.partitionMananger = partitionManager;
    }

    public EventContext getContext() {
        return context;
    }

    public PartitionManager getPartitionMananger() {
        return this.partitionMananger;
    }
}
