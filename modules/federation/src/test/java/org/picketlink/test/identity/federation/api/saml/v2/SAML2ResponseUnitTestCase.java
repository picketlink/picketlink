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

import org.jboss.logging.Logger;
import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.test.identity.federation.api.util.KeyUtilUnitTestCase;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test the {@link SAML2Response} API
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 21, 2011
 */
public class SAML2ResponseUnitTestCase {

    private final String keystoreLocation = "keystore/jbid_test_keystore.jks";

    private final String keystorePass = "store123";

    private final String keyPass = "test123";

    private final String alias = "servercert";

    /**
     * Parse a {@link ResponseType} that contains ADFS Claims and then try to sign
     *
     * @throws Exception
     */
    @Test
    public void parseADFSClaims() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("saml/v2/response/saml2-response-adfs-claims.xml");
        SAML2Response samlResponse = new SAML2Response();
        SAML2Object samlObject = samlResponse.getSAML2ObjectFromStream(configStream);
        assertNotNull(samlObject);

        SAML2Signature sig = new SAML2Signature();
        Document signedDoc = sig.sign((ResponseType) samlObject, getKeyPair());
        assertNotNull(signedDoc);

        Logger.getLogger(SAML2ResponseUnitTestCase.class).debug("Signed Response=" + DocumentUtil.asString(signedDoc));
    }

    /**
     * This test constructs the {@link ResponseType}. An {@link AssertionType} is locally constructed and then passed to
     * the
     * construct method
     *
     * @throws Exception
     */
    @Test
    public void constructAndSign() throws Exception {
        SAML2Response samlResponse = new SAML2Response();
        String ID = IDGenerator.create("ID_");

        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("picketlink");

        IDPInfoHolder idp = new IDPInfoHolder();
        idp.setNameIDFormatValue("anil");

        // create the service provider(in this case BAS) holder object
        SPInfoHolder sp = new SPInfoHolder();
        sp.setResponseDestinationURI("http://sombody");

        Map<String, Object> attributes = new HashMap<String, Object>();

        attributes.put("TOKEN_USER_ID", String.valueOf(2));
        attributes.put("TOKEN_ORGANIZATION_DISPLAY_NAME", "Test Org");
        attributes.put("TOKEN_USER_DISPLAY_NAME", "Test User");

        AttributeStatementType attributeStatement = StatementUtil.createAttributeStatement(attributes);

        String assertionId = IDGenerator.create("ID_");

        AssertionType assertion = AssertionUtil.createAssertion(assertionId, issuerInfo.getIssuer());
        assertion.addStatement(attributeStatement);

        ResponseType responseType = samlResponse.createResponseType(ID, sp, idp, issuerInfo, assertion);
        SAML2Signature sig = new SAML2Signature();
        Document signedDoc = sig.sign(responseType, getKeyPair());
        assertNotNull(signedDoc);

        Logger.getLogger(SAML2ResponseUnitTestCase.class).debug("Signed Response=" + DocumentUtil.asString(signedDoc));

        Document convertedDoc = samlResponse.convert(responseType);
        assertNotNull(convertedDoc);

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SAMLResponseWriter samlWriter = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        samlWriter.write(responseType);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

    /**
     * @return
     *
     * @throws Exception
     * @see {@link KeyUtilUnitTestCase}
     */
    private KeyPair getKeyPair() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
        assertNotNull("Input keystore stream is not null", ksStream);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(ksStream, keystorePass.toCharArray());
        assertNotNull("KeyStore is not null", ks);

        Certificate cert = ks.getCertificate(alias);
        assertNotNull("Cert not null", cert);

        // Get private key
        Key key = ks.getKey(alias, keyPass.toCharArray());
        return new KeyPair(cert.getPublicKey(), (PrivateKey) key);
    }
}