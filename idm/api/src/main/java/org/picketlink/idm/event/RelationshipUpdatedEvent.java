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

import org.picketlink.idm.model.Relationship;

/**
 * This event is raised when a new {@link Relationship} is updated
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RelationshipUpdatedEvent extends AbstractBaseEvent {
    
    private Relationship relationship;

    public RelationshipUpdatedEvent(Relationship relationship) {
        this.relationship = relationship;
    }
    
    public Relationship getRelationship() {
        return relationship;
    }
}
