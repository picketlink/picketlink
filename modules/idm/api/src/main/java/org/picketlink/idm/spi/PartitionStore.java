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
package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Partition;

import java.util.List;

/**
 * A special type of IdentityStore that is also capable of providing partition management functionality
 *
 * @author Shane Bryzak
 *
 */
public interface PartitionStore<T extends IdentityStoreConfiguration> extends IdentityStore<T> {

    String getConfigurationName(IdentityContext identityContext, Partition partition);

    <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name);

    <P extends Partition> List<P> get(IdentityContext identityContext, Class<P> partitionClass);

    <P extends Partition> P lookupById(final IdentityContext context, final Class<P> partitionClass, String id);

    void add(IdentityContext identityContext, Partition partition, String configurationName);

    void update(IdentityContext identityContext, Partition partition);

    void remove(IdentityContext identityContext, Partition partition);
}
