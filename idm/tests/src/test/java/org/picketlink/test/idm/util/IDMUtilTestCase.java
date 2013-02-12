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
package org.picketlink.test.idm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.picketlink.idm.internal.util.IDMUtil;
import org.junit.Test;

/**
 * Unit test {@link IDMUtil}
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class IDMUtilTestCase {
    @Test
    public void arrMatch() throws Exception {
        String[] a1 = { "1", "2", "3", "4" };
        String[] a2 = { "1", "3", "4", "2", };
        String[] a3 = { "2", "3", "4" };
        String[] a4 = { "3", "2", "4" };

        assertTrue(IDMUtil.arraysEqual(a1, a2));
        assertTrue(IDMUtil.arraysEqual(a3, a4));
        assertFalse(IDMUtil.arraysEqual(a1, a4));
        assertFalse(IDMUtil.arraysEqual(a2, a3));
    }
}
