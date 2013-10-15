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
package org.picketlink.test.identity.federation.core.util;

import org.junit.Test;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.StringUtil;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test {@link StringUtil}
 *
 * @author Anil Saldhana
 * @since Feb 22, 2012
 */
public class StringUtilTestCase {

    @Test
    public void testTokenizeKeyValuePairs() throws Exception {
        String str = "a=b,c=d,e=f";
        Map<String, String> map = StringUtil.tokenizeKeyValuePair(str);

        assertEquals("b", map.get("a"));
        assertEquals("d", map.get("c"));
        assertEquals("f", map.get("e"));
    }

    @Test
    public void testTokenize() throws Exception {
        String str = "a,b";

        String theOtherString = "a/b;c/d";

        List<String> list = StringUtil.tokenize(str);
        assertEquals(2, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));

        List<String> bigList = StringUtil.tokenize(theOtherString, ";");
        assertEquals(2, bigList.size());
        for (String token : bigList) {
            List<String> theList = StringUtil.tokenize(token, "/");

            if (token.equals("a/b")) {
                assertTrue(theList.contains("a"));
                assertTrue(theList.contains("b"));
            } else if (token.equals("c/d")) {
                assertTrue(theList.contains("c"));
                assertTrue(theList.contains("d"));
            } else
                throw new RuntimeException("Unknown");
        }
    }

    @Test
    public void trim() throws Exception {
        assertNotNull("".trim());
        assertEquals(0, "".trim().length());
        assertEquals(0, StaxParserUtil.trim("").length());
    }
}