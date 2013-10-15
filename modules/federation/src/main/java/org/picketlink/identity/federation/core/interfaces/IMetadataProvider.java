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
package org.picketlink.identity.federation.core.interfaces;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Map;

/**
 * MetadataProvider
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 21, 2009
 */
public interface IMetadataProvider<T> {

    /**
     * Initialize the provider with options
     *
     * @param options
     */
    void init(Map<String, String> options);

    /**
     * Is multiple descriptors attached?
     *
     * @return
     */
    boolean isMultiple();

    /**
     * Get the Metadata descriptors
     *
     * @return
     */
    T getMetaData();

    /**
     * Provider indicates that it requires an injection of File instance
     *
     * @return File Name (need injection) or null
     */
    String requireFileInjection();

    /**
     * Inject a File instance depending on
     *
     * @param fileStream
     *
     * @see #requireFileInjection() method
     */
    void injectFileStream(InputStream fileStream);

    /**
     * Inject a public key used for signing
     *
     * @param publicKey
     */
    void injectSigningKey(PublicKey publicKey);

    /**
     * Inject a public key used for encryption
     *
     * @param publicKey
     */
    void injectEncryptionKey(PublicKey publicKey);
}