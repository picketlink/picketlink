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
package org.picketlink.identity.federation.core.sts.registry;

/**
 * <p>
 * A {@code RevocationRegistry} is used to store the ids of revoked (canceled) security tokens.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface RevocationRegistry {

    /**
     * <p>
     * Indicates whether the token with the specified id has been revoked or not.
     * </p>
     *
     * @param tokenType a {@code String} representing the token type.
     * @param id a {@code String} representing the token id.
     *
     * @return {@code true} if the specified id has been revoked; {@code false} otherwise.
     */
    boolean isRevoked(String tokenType, String id);

    /**
     * <p>
     * Adds the specified id to the revocation registry. The security token type can be used to distinguish tokens that
     * may have
     * the same id but that are of different types.
     * </p>
     *
     * @param tokenType a {@code String} representing the security token type.
     * @param id the id to registered.
     */
    void revokeToken(String tokenType, String id);
}
