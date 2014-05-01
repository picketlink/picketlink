package org.picketlink.test.identity.federation.api.util;

import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.w3c.dom.Document;

import static junit.framework.Assert.assertTrue;

public class IDPWebRequestUtilTest {

    @Test
    public void testGetErrorResponse() throws Exception {
        IDPType idpType = new IDPType();

        idpType.setIdentityURL("http://idp.picketlink.org");

        IDPWebRequestUtil idpWebRequestUtil = new IDPWebRequestUtil(new MockHttpServletRequest("GET"), idpType, null);

        Document errorResponse = idpWebRequestUtil
            .getErrorResponse("http://sp.picketlink.org", JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), idpType
                .getIdentityURL(), false);

        String errorResponseStr = DocumentUtil.asString(errorResponse);

        assertTrue(errorResponseStr
            .contains("<samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Responder\"><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:AuthnFailed\"/></samlp:StatusCode></samlp:Status>"));
    }
}