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
package org.picketlink.test.identity.federation.api.saml.v2;

import junit.framework.TestCase;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;

/**
 * Unit Test the SAMl2Request API
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 26, 2009
 */
public class SAML2RequestUnitTestCase extends TestCase {

    public void testLogOut() throws Exception {
        SAML2Request saml2Request = new SAML2Request();
        LogoutRequestType lrt = saml2Request.createLogoutRequest("http://idp");
        assertNotNull("LogoutRequest is not null", lrt);
    }

}