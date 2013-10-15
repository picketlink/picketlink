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
package org.picketlink.test.identity.federation.core.parser.wst;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;

import java.io.InputStream;

import static junit.framework.Assert.assertNotNull;

/**
 * PLINK-144 : PicketLink STS should not choke on WS-Policy 1.5 Tags
 *
 * @author Anil Saldhana
 * @since May 20, 2013
 */
public class WSTrustRSTWithWSPolicy15TestCase {

    @Test
    public void parseRSTWithWSP_15() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-rst-wsp15.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);
        assertNotNull(requestToken);
    }
}