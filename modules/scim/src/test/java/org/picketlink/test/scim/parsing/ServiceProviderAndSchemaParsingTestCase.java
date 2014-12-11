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
import org.picketlink.scim.model.v11.SCIMMetaData;
import org.picketlink.scim.model.v11.parser.SCIMParser;
import org.picketlink.scim.model.v11.resource.ServiceProviderConfiguration;
import org.picketlink.scim.model.v11.resource.ServiceProviderConfiguration.Bulk;
import org.picketlink.scim.model.v11.resource.ServiceProviderConfiguration.Filter;
import org.picketlink.scim.model.v11.schema.SCIMSchema;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Validate parsing of SCIM Service Provider Configuration representation
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class ServiceProviderAndSchemaParsingTestCase {

    @Test
    public void parseServiceProvider() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/serviceprovider.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        ServiceProviderConfiguration sp = parser.parseServiceProviderConfiguration(is);
        assertNotNull(sp);

        assertEquals("http://example.com/help/scim.html", sp.getDocumentationUrl());

        assertTrue(sp.getPatch().isSupported());
        assertEquals(2, sp.getAuthenticationSchemes().length);

        Bulk bulk = sp.getBulk();
        assertTrue(bulk.isSupported());
        assertEquals(1000, bulk.getMaxOperations());
        assertEquals(1048576, bulk.getMaxPayloadSize());

        Filter filter = sp.getFilter();
        assertTrue(filter.isSupported());
        assertEquals(200, filter.getMaxResults());

        assertTrue(sp.getChangePassword().isSupported());
        assertTrue(sp.getSort().isSupported());
        assertTrue(sp.getEtag().isSupported());
    }
    
    @Test
    public void parseSchema() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/schema.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        SCIMSchema[] schemas = parser.parseSchema(is);
        assertEquals(3, schemas.length);
        assertEquals(schemas[0].getId(), "urn:ietf:params:scim:schemas:core:2.0:User");
        assertEquals(schemas[0].getName(), "User");
        assertEquals(schemas[0].getDescription(), "User Account");

        SCIMSchema.Attribute attributes[] = schemas[0].getAttributes();
        assertEquals(21, attributes.length);
        assertEquals(attributes[0].getName(), "userName");
        assertEquals(attributes[0].getType(), "string");
        assertEquals(attributes[0].isMultiValued(), false);
        assertEquals(
            attributes[0].getDescription(),
            "Unique identifier for the User typically used by the user to directly authenticate to the service provider. Each User MUST include a non-empty userName value.  This identifier MUST be unique across the Service Consumer's entire set of Users.  REQUIRED");
        assertEquals(attributes[0].isRequired(), true);
        assertEquals(attributes[0].isCaseExact(), false);
        assertEquals(attributes[0].getMutability(), "readWrite");
        assertEquals(attributes[0].getReturned(), "default");
        assertEquals(attributes[0].getUniqueness(), "server");

        assertEquals(attributes[1].getName(), "name");
        assertEquals(attributes[1].getType(), "complex");
        assertEquals(attributes[1].isMultiValued(), false);
        assertEquals(
                attributes[1].getDescription(),
                "The components of the user's real name. Providers MAY return just the full name as a single string in the formatted sub-attribute, or they MAY return just the individual component attributes using the other sub-attributes, or they MAY return both. If both variants are returned, they SHOULD be describing the same name, with the formatted name indicating how the component attributes should be combined.");
        assertEquals(attributes[1].isRequired(), false);
        assertEquals(attributes[1].isCaseExact(), false);
        assertEquals(attributes[1].getMutability(), "readWrite");
        assertEquals(attributes[1].getReturned(), "default");
        assertEquals(attributes[1].getUniqueness(), "none");

        SCIMSchema.BasicAttribute subAttributes[] = attributes[1].getSubAttributes();
        assertEquals(6, subAttributes.length);
        assertEquals(subAttributes[0].getName(), "formatted");
        assertEquals(subAttributes[0].getType(), "string");
        assertEquals(subAttributes[0].isMultiValued(), false);
        assertEquals(
                subAttributes[0].getDescription(),
                "The full name, including all middle names, titles, and suffixes as appropriate, formatted for display (e.g. Ms. Barbara J Jensen, III.).");
        assertEquals(subAttributes[0].isRequired(), false);
        assertEquals(subAttributes[0].isCaseExact(), false);
        assertEquals(subAttributes[0].getMutability(), "readWrite");
        assertEquals(subAttributes[0].getReturned(), "default");
        assertEquals(subAttributes[0].getUniqueness(), "none");

        subAttributes = attributes[12].getSubAttributes();
        String canonicalValues[] = subAttributes[2].getCanonicalValues();
        assertEquals(canonicalValues.length, 3);
        assertEquals(canonicalValues[0], "work");
        assertEquals(canonicalValues[1], "home");
        assertEquals(canonicalValues[2], "other");
        
        SCIMMetaData meta = schemas[0].getMeta();
        assertEquals(meta.getCreated(), "2010-01-23T04:56:22Z");
        assertEquals(meta.getLastModified(), "2014-02-04T00:00:00Z");
        assertEquals(meta.getLocation(), "https://example.com/v2/Schemas/urn:ietf:params:scim:schemas:core:2.0:User");
        assertEquals(meta.getResourceType(), "Schema");
        assertEquals(meta.getVersion(), "W/\"3694e05e9dff596\"");

    }
}