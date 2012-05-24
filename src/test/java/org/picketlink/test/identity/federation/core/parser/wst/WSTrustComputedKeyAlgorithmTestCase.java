package org.picketlink.test.identity.federation.core.parser.wst;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.w3c.dom.Document;

/**
 * Unit test the wst:ComputedKeyAlgorithm
 *
 * @author anil saldhana
 */
public class WSTrustComputedKeyAlgorithmTestCase {

    @Test
    public void testComputedKeyAlgorithm() throws Exception {

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-computedkeyalgorithm.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);
        assertEquals("http://docs.oasis-open.org/ws-sx/ws-trust/200512/CK/PSHA1", requestToken.getComputedKeyAlgorithm().toASCIIString());
        
        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        byte[] data = baos.toByteArray();
        Logger.getLogger(WSTrustComputedKeyAlgorithmTestCase.class).debug(new String(data));
        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(data));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }
}