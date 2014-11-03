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
package org.picketlink.test.scim.parsing;

import org.junit.Test;
import org.picketlink.scim.codec.SCIMParser;
import org.picketlink.scim.model.v11.SCIMResource;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate parsing of SCIM Resource representation
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class ResourceParsingTestCase {

    @Test
    public void parse() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/resource.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        SCIMResource resource = parser.parseResource(is);
        assertNotNull(resource);

        assertEquals("User", resource.getId());
        assertEquals("User", resource.getName());

        assertEquals("User Account", resource.getDescription());
        assertEquals("urn:ietf:params:scim:schemas:core:2.0:User", resource.getSchema());
        assertEquals("/Users", resource.getEndpoint());
    }
}