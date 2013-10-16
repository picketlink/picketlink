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

import junit.framework.Assert;
import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;

import java.io.ByteArrayOutputStream;

/**
 * Unit Test the SAML2 Authn Response factory
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class SAML2AuthnResponseUnitTestCase {

    @Test
    public void testResponseTypeCreation() throws Exception {
        // Initialize the Core STS
        PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
        sts.installDefaultConfiguration();

        IssuerInfoHolder issuerHolder = new IssuerInfoHolder("http://idp");
        issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());

        IDPInfoHolder idp = new IDPInfoHolder();
        idp.setNameIDFormatValue(IDGenerator.create());

        SAML2Response saml2Response = new SAML2Response();

        SPInfoHolder sp = new SPInfoHolder();
        sp.setResponseDestinationURI("http://fakesp");
        sp.setIssuer("http://fakesp");
        ResponseType rt = saml2Response.createResponseType("response111", sp, idp, issuerHolder);
        Assert.assertNotNull(rt);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        saml2Response.marshall(rt, baos);
    }
}