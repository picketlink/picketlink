package org.picketlink.test.identity.federation.core.parser.wst;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.w3c.dom.Document;

/**
 * Unit test the wst:SecondaryParameters
 *
 * @author anil saldhana
 */
public class WSTrustSecondaryParametersTestCase {

    @Test
    public void testSecondaryParameters() throws Exception {

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-secondaryparameters.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);

        EndpointReferenceType endpoint = (EndpointReferenceType) requestToken.getAppliesTo().getAny().get(0);
        assertEquals("http://localhost:8080/jaxws-samples-wsse-policy-trust/SecurityService", endpoint.getAddress().getValue());
        assertEquals(WSTrustConstants.ISSUE_REQUEST, requestToken.getRequestType().toASCIIString());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, requestToken.getTokenType().toASCIIString());

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        byte[] data = baos.toByteArray();
        System.out.println(new String(data));
        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(data));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

}