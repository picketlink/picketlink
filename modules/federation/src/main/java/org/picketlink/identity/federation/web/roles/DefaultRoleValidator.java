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
package org.picketlink.identity.federation.web.roles;

import org.jboss.logging.Logger;
import org.picketlink.identity.federation.web.interfaces.IRoleValidator;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class DefaultRoleValidator implements IRoleValidator {

    private static Logger log = Logger.getLogger(DefaultRoleValidator.class);
    private boolean trace = log.isTraceEnabled();

    private Set<String> roleNames = new HashSet<String>();

    public void intialize(Map<String, String> options) {
        String csv = options.get("ROLES");
        if (csv == null) {
            if (trace)
                log.trace("There is no ROLES config");
        } else {
            // Get the comma separated role names
            StringTokenizer st = new StringTokenizer(csv, ",");
            while (st != null && st.hasMoreTokens()) {
                roleNames.add(st.nextToken());
            }
        }
    }

    public boolean userInRole(Principal userPrincipal, List<String> roles) {
        for (String roleName : roles) {
            if (roleNames.contains(roleName))
                return true;
        }
        return false;
    }
}