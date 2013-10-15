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
package org.picketlink.test.identity.federation.core.parser;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.common.util.StringUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit Test {@link StringUtil#getSystemPropertyAsString(String)} that parses a string that represents a system
 * property
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 22, 2011
 */
public class SystemPropertyAsStringUnitTestCase {

    @Before
    public void setup() {
        System.setProperty("test", "anil");
        System.setProperty("person", "marcus");
    }

    @Test
    public void testSystemProperty() throws Exception {
        try {
            assertEquals(null, StringUtil.getSystemPropertyAsString(null));
            fail("should not have passed");
        } catch (IllegalArgumentException iae) {

        } catch (Exception e) {
            fail("unknown ex");
        }
        assertEquals("test", StringUtil.getSystemPropertyAsString("test"));
        assertEquals("test/test", StringUtil.getSystemPropertyAsString("test/test"));

        assertEquals("anil", StringUtil.getSystemPropertyAsString("${test::something}"));

        assertEquals("anil", StringUtil.getSystemPropertyAsString("${test}"));
        assertEquals("test/anil", StringUtil.getSystemPropertyAsString("test/${test}"));

        assertEquals("anil:anil:marcus//anil", StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test}"));

        // Test if any of the parantheses are not correctly closed
        assertEquals("anil:anil:marcus//${test", StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test"));

        // Test the default values
        assertEquals("http://something", StringUtil.getSystemPropertyAsString("${dummy::http://something}"));
        assertEquals("http://something__hi", StringUtil.getSystemPropertyAsString("${dummy::http://something}__${to::hi}"));
        assertEquals("anil:anil:marcus//anilhi",
                StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test}${to::hi}"));
        assertEquals("anil:anil:marcus//anilhihttp://something",
                StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test}${to::hi}${dummy::http://something}"));
    }
}