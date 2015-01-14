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
package org.picketlink.idm.internal;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;

/**
 * @author pedroigor
 */
public class DefaultIdentityContext extends AbstractIdentityContext {

    public DefaultIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator) {
        super(partition, eventBridge, idGenerator);
    }

    public DefaultIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator, PermissionHandlerPolicy permissionHandlerPolicy) {
        super(partition, eventBridge, idGenerator, permissionHandlerPolicy);
    }
}
