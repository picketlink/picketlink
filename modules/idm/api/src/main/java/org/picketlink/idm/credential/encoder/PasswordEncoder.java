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

package org.picketlink.idm.credential.encoder;

/**
 * <p>
 * Represents different forms to encode passwords.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public interface PasswordEncoder {

    /**
     * <p>Encodes given password.</p>
     *
     * @param rawPassword
     * @return
     */
    String encode(String rawPassword);

    /**
     * <p>Checks if the <code>rawPassword</code> and <code>encodedPassword</code> matches.</p>
     *
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    boolean verify(String rawPassword, String encodedPassword);
}