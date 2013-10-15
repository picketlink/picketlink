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
package org.picketlink.identity.federation.web.interfaces;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Validate the passed Roles
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public interface IRoleValidator {

    /**
     * Initialize the validator
     *
     * @param options
     */
    void intialize(Map<String, String> options);

    /**
     * Validate whether the principal with the given list of roles is valid
     *
     * @param userPrincipal
     * @param roles
     *
     * @return
     */
    boolean userInRole(Principal userPrincipal, List<String> roles);
}