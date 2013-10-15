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
package org.picketlink.identity.federation.core.impl;

import org.picketlink.identity.federation.core.interfaces.RoleGenerator;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * A Role Generator that generates no roles
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class EmptyRoleGenerator implements RoleGenerator {

    /**
     * @see {@code RoleGenerator#generateRoles(Principal)}
     */
    public List<String> generateRoles(Principal principal) {
        return new ArrayList<String>();
    }
}