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
package org.picketlink.identity.federation.web.util;

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;

/**
 * Returns configuration for an IDP or SP
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 9, 2011
 */
public interface SAMLConfigurationProvider {

    /**
     * Get the {@link IDPType} configuration
     *
     * @return
     *
     * @throws ProcessingException
     */
    IDPType getIDPConfiguration() throws ProcessingException;

    /**
     * Get the {@l SPType} configuration
     *
     * @return
     *
     * @throws ProcessingException
     */
    SPType getSPConfiguration() throws ProcessingException;

    /**
     * Get the {@l SPType} configuration
     *
     * @return
     *
     * @throws ProcessingException
     */
    PicketLinkType getPicketLinkConfiguration() throws ProcessingException;
}