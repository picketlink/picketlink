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
package org.picketlink.test.json;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picketlink.json.util.HmacSha256Util;

/**
 * Unit test the {@link HmacSha256Util}
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class HmacSha256UtilTestCase {
    @Test
    public void encode() throws Exception {
        String payload = "hi. hello.how are you?";
        assertNotNull(HmacSha256Util.encode(payload));
    }
}
