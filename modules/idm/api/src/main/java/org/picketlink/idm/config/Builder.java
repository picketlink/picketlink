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
package org.picketlink.idm.config;

/**
 * <p>Base class for builder implementations.</p>
 *
 * @author pedroigor
 */
public abstract class Builder<T> {

    /**
     * <p>Creates a new configuration.</p>
     *
     * @return
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    protected abstract T create() throws SecurityConfigurationException ;

    /**
     * <p>Validates the internal state.</p>
     *
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    protected abstract void validate() throws SecurityConfigurationException;

    /**
     * <p>Reads a pre-created configuration.</p>
     *
     * @param fromConfiguration
     * @return
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    protected abstract Builder<T> readFrom(T fromConfiguration) throws SecurityConfigurationException ;

}
