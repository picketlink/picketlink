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

import java.io.IOException;

/**
 * A registry of Security Tokens that may be issued by instances of {@code SecurityTokenProvider}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public interface SecurityTokenRegistry {

    /**
     * Add a token to the registry with the given id
     *
     * @param tokenID
     * @param token
     *
     * @throws {@code IOException}
     */
    void addToken(String tokenID, Object token) throws IOException;

    /**
     * Remove a token given the ID
     *
     * @param tokenID
     * @param token
     *
     * @throws {@code IOException}
     */
    void removeToken(String tokenID) throws IOException;

    /**
     * Given the id, return a token
     *
     * @param tokenID
     *
     * @return
     */
    Object getToken(String tokenID);
}