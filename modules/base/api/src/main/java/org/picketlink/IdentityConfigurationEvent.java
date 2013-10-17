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

import org.picketlink.idm.config.IdentityConfigurationBuilder;

/**
 * <p>This event is raised during PicketLink startup when building the IDM configuration.</p>
 *
 * <p>Observers can handle this event in order to get any additional configuration added to the {@link
 * IdentityConfigurationBuilder}</p> instance that will be used to build the IDM configuration.</p>
 *
 * <p>The state of the {@link IdentityConfigurationBuilder} depends on the following situations:</p>
 *
 * <ul>
 * <li>If no {@link org.picketlink.idm.config.IdentityConfiguration} was produced, the builder is just an empty
 * instance, with no configuration.</li>
 * <li>If any {@link org.picketlink.idm.config.IdentityConfiguration} was produced, the builder is already
 * populated with those configurations.</li>
 * </ul>
 *
 * <p>If the observer of this event does not provide any additional configuration and no {@link org.picketlink.idm
 * .config.IdentityConfiguration} was produced, the default configuration will be used.</p>
 *
 * @author Shane Bryzak
 */
public class IdentityConfigurationEvent {

    private IdentityConfigurationBuilder config;

    public IdentityConfigurationEvent(IdentityConfigurationBuilder config) {
        this.config = config;
    }

    public IdentityConfigurationBuilder getConfig() {
        return config;
    }
}