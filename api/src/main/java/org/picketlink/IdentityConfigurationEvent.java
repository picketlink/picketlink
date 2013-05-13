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
 * This event is raised during PicketLink configuration 
 * 
 * @author Shane Bryzak
 *
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
