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
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMGroups.Members;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Validate parsing of SCIM Group representation
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class GroupParsingTestCase {

    @Test
    public void parse() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/group.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        SCIMGroups group = parser.parseGroup(is);
        assertNotNull(group);

        assertEquals("e9e30dba-f08f-4109-8486-d5c6a331660a", group.getId());
        assertEquals("Tour Guides", group.getDisplayName());

        Members[] members = group.getMembers();
        assertEquals(2, members.length);

        // Validate members
        for (int i = 0; i < 2; i++) {
            Members member = members[i];
            assertTrue("2819c223-7f76-453a-919d-413861904646".equals(member.getValue())
                    || "902c246b-6245-4190-8e05-00816be7344a".equals(member.getValue()));
            assertTrue("Babs Jensen".equals(member.getDisplay()) || "Mandy Pepperidge".equals(member.getDisplay()));
        }
    }
}