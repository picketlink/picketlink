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

package org.picketlink;

import org.picketlink.idm.PartitionManager;

/**
 * <p>This event is fired during PicketLink startup after creating the {@link PartitionManager}.</p>
 *
 * <p>Observers can handle this event in order to perform any initialization right after the {@link PartitionManager} was built.</p>
 *
 * @author Pedro Igor
 */
public class PartitionManagerCreateEvent {

    private final PartitionManager partitionManager;

    public PartitionManagerCreateEvent(final PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    public PartitionManager getPartitionManager() {
        return this.partitionManager;
    }
}