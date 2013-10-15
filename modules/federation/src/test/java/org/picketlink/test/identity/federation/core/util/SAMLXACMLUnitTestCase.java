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

import org.jboss.security.xacml.core.model.context.RequestType;
import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Read a SAML-XACML request
 *
 * @author Anil.Saldhana@redhat.com
 * @see {@code SAMLResponseParserTestCase#testXACMLDecisionStatements()}
 * @since Jan 8, 2009
 */
public class SAMLXACMLUnitTestCase {

    /**
     * Usage of samlp with xsi-type
     */
    @Test
    public void testSAML_XACML_Read() throws Exception {
        String resourceName = "saml-xacml/saml-xacml-request.xml";

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(resourceName);

        SAMLParser parser = new SAMLParser();
        RequestAbstractType req = (RequestAbstractType) parser.parse(is);
        assertNotNull(req);
        assertTrue(req instanceof XACMLAuthzDecisionQueryType);

        XACMLAuthzDecisionQueryType xadqt = (XACMLAuthzDecisionQueryType) req;
        RequestType requestType = xadqt.getRequest();
        assertNotNull(requestType);
    }

    /**
     * Usage of xacml-samlp
     */
    @Test
    public void testSAML_XACML_Read_2() throws Exception {
        String resourceName = "saml-xacml/saml-xacml-request-2.xml";

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(resourceName);

        SAMLParser parser = new SAMLParser();
        RequestAbstractType req = (RequestAbstractType) parser.parse(is);
        assertNotNull(req);
        assertTrue(req instanceof XACMLAuthzDecisionQueryType);

        XACMLAuthzDecisionQueryType xadqt = (XACMLAuthzDecisionQueryType) req;
        RequestType requestType = xadqt.getRequest();
        assertNotNull(requestType);
    }
}