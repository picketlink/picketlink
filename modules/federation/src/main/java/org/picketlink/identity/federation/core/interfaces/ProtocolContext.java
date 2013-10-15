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

import javax.xml.namespace.QName;

/**
 * Interface to indicate a protocol specific request context
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public interface ProtocolContext {

    /**
     * An optional service name
     *
     * @return
     */
    String serviceName();

    /**
     * A String that represents the token type
     *
     * @return
     */
    String tokenType();

    /**
     * Return the QName of the token
     *
     * @return
     */
    QName getQName();

    /**
     * What family the context belongs to..
     *
     * @return
     *
     * @see {@code SecurityTokenProvider#family()}
     * @see {@code FAMILY_TYPE}
     */
    String family();
}