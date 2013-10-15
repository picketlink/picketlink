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

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Simple Role Generator that looks inside a roles.properties on the classpath with format: principalName=role1,role2
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class DefaultRoleGenerator implements RoleGenerator {

    private static Properties props = new Properties();

    static {
        try {
            URL url = SecurityActions.loadResource(DefaultRoleGenerator.class, "roles.properties");
            if (url == null)
                throw new RuntimeException(ErrorCodes.RESOURCE_NOT_FOUND + "roles.properties not found");
            props.load(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> generateRoles(Principal principal) {
        List<String> roles = new ArrayList<String>();

        String csv = (String) props.get(principal.getName());
        if (StringUtil.isNotNull(csv)) {
            roles.addAll(StringUtil.tokenize(csv));
        }
        return roles;
    }
}