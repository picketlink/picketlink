/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.web.util;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Signature Support for the HTTP/Redirect binding
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 16, 2008
 */
public class RedirectBindingSignatureUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Get the URL for the SAML request that contains the signature and signature algorithm
     *
     * @param authRequest
     * @param relayState
     * @param signingKey
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String getSAMLRequestURLWithSignature(AuthnRequestType authRequest, String relayState, PrivateKey signingKey)
            throws SAXException, IOException, GeneralSecurityException {
        SAML2Request saml2Request = new SAML2Request();

        // Deal with the original request
        StringWriter sw = new StringWriter();

        saml2Request.marshall(authRequest, sw);

        // URL Encode the Request
        String urlEncodedRequest = RedirectBindingUtil.deflateBase64URLEncode(sw.toString());

        String urlEncodedRelayState = null;

        if (isNotNull(relayState))
            urlEncodedRelayState = URLEncoder.encode(relayState, "UTF-8");

        byte[] sigValue = computeSignature(GeneralConstants.SAML_REQUEST_KEY, urlEncodedRequest, urlEncodedRelayState,
                signingKey);

        // Now construct the URL
        return getRequestRedirectURLWithSignature(urlEncodedRequest, urlEncodedRelayState, sigValue, signingKey.getAlgorithm());
    }

    /**
     * Get the URL for the SAML request that contains the signature and signature algorithm
     *
     * @param responseType
     * @param relayState
     * @param signingKey
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static String getSAMLResponseURLWithSignature(ResponseType responseType, String relayState, PrivateKey signingKey)
            throws IOException, GeneralSecurityException {
        SAML2Response saml2Response = new SAML2Response();

        Document responseDoc = saml2Response.convert(responseType);

        // URL Encode the Request
        String responseString = DocumentUtil.getDocumentAsString(responseDoc);

        String urlEncodedResponse = RedirectBindingUtil.deflateBase64URLEncode(responseString);

        String urlEncodedRelayState = null;
        if (isNotNull(relayState))
            urlEncodedRelayState = URLEncoder.encode(relayState, "UTF-8");

        byte[] sigValue = computeSignature(GeneralConstants.SAML_RESPONSE_KEY, urlEncodedResponse, urlEncodedRelayState,
                signingKey);

        // Now construct the URL
        return getResponseRedirectURLWithSignature(urlEncodedResponse, urlEncodedRelayState, sigValue,
                signingKey.getAlgorithm());
    }

    /**
     * Given an url-encoded saml request and relay state and a private key, compute the url
     *
     * @param urlEncodedRequest
     * @param urlEncodedRelayState
     * @param signingKey
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static String getSAMLRequestURLWithSignature(String urlEncodedRequest, String urlEncodedRelayState,
            PrivateKey signingKey) throws IOException, GeneralSecurityException {
        byte[] sigValue = computeSignature(GeneralConstants.SAML_REQUEST_KEY, urlEncodedRequest, urlEncodedRelayState,
                signingKey);
        return getRequestRedirectURLWithSignature(urlEncodedRequest, urlEncodedRelayState, sigValue, signingKey.getAlgorithm());
    }

    /**
     * Given an url-encoded saml response and relay state and a private key, compute the url
     *
     * @param urlEncodedResponse
     * @param urlEncodedRelayState
     * @param signingKey
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static String getSAMLResponseURLWithSignature(String urlEncodedResponse, String urlEncodedRelayState,
            PrivateKey signingKey) throws IOException, GeneralSecurityException {
        byte[] sigValue = computeSignature(GeneralConstants.SAML_RESPONSE_KEY, urlEncodedResponse, urlEncodedRelayState,
                signingKey);
        return getResponseRedirectURLWithSignature(urlEncodedResponse, urlEncodedRelayState, sigValue,
                signingKey.getAlgorithm());
    }

    /**
     * From the SAML Request URL, get the Request object
     *
     * @param signedURL
     * @return
     * @throws IOException
     * @throws ParsingException
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    public static AuthnRequestType getRequestFromSignedURL(String signedURL) throws ConfigurationException,
            ProcessingException, ParsingException, IOException {
        String samlRequestTokenValue = getTokenValue(signedURL, GeneralConstants.SAML_REQUEST_KEY);

        SAML2Request saml2Request = new SAML2Request();
        return saml2Request.getAuthnRequestType(RedirectBindingUtil.urlBase64DeflateDecode(samlRequestTokenValue));
    }

    /**
     * Get the signature value from the url
     *
     * @param signedURL
     * @return
     * @throws IOException
     */
    public static byte[] getSignatureValueFromSignedURL(String signedURL) throws IOException {
        String sigValueTokenValue = getTokenValue(signedURL, GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        if (sigValueTokenValue == null)
            throw new IllegalStateException(logger.samlHandlerSignatureNotPresentError());
        return RedirectBindingUtil.urlBase64Decode(sigValueTokenValue);
    }

    /**
     * From the query string that contains key/value pairs, get the value of a key <b>Note:</b> if the token is null, a null
     * value is returned
     *
     * @param queryString
     * @param token
     * @return
     */
    public static String getTokenValue(String queryString, String token) {
        return getTokenValue(getToken(queryString, token));
    }

    public static boolean validateSignature(String queryString, PublicKey validatingKey, byte[] sigValue)
            throws UnsupportedEncodingException, GeneralSecurityException {
        // Construct the url again
        StringBuilder sb = new StringBuilder();

        if (isRequestQueryString(queryString)) {
            addParameter(sb, GeneralConstants.SAML_REQUEST_KEY,
                    RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.SAML_REQUEST_KEY));
        } else {
            addParameter(sb, GeneralConstants.SAML_RESPONSE_KEY,
                    RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.SAML_RESPONSE_KEY));
        }

        String relayStateFromURL = RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.RELAY_STATE);

        if (isNotNull(relayStateFromURL)) {
            addParameter(sb, GeneralConstants.RELAY_STATE, relayStateFromURL);
        }

        addParameter(sb, GeneralConstants.SAML_SIG_ALG_REQUEST_KEY,
                RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.SAML_SIG_ALG_REQUEST_KEY));

        return SignatureUtil.validate(sb.toString().getBytes("UTF-8"), sigValue, validatingKey);
    }

    private static boolean isRequestQueryString(String queryString) {
        return RedirectBindingSignatureUtil.getTokenValue(queryString, GeneralConstants.SAML_REQUEST_KEY) != null;
    }

    // ***************** Private Methods **************

    private static byte[] computeSignature(String samlParameter, String urlEncoded, String urlEncodedRelayState,
            PrivateKey signingKey) throws IOException, GeneralSecurityException {
        StringBuilder sb = new StringBuilder();

        addParameter(sb, samlParameter, urlEncoded);

        if (isNotNull(urlEncodedRelayState)) {
            addParameter(sb, GeneralConstants.RELAY_STATE, urlEncodedRelayState);
        }

        // SigAlg
        String algo = signingKey.getAlgorithm();
        String sigAlg = SignatureUtil.getXMLSignatureAlgorithmURI(algo);

        sigAlg = URLEncoder.encode(sigAlg, "UTF-8");

        addParameter(sb, GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, sigAlg);

        byte[] sigValue = SignatureUtil.sign(sb.toString(), signingKey);

        return sigValue;
    }

    private static String getRequestRedirectURLWithSignature(String urlEncodedRequest, String urlEncodedRelayState,
            byte[] signature, String sigAlgo) throws IOException {
        return getRedirectURLWithSignature(GeneralConstants.SAML_REQUEST_KEY, urlEncodedRequest, urlEncodedRelayState,
                signature, sigAlgo);
    }

    private static String getResponseRedirectURLWithSignature(String urlEncodedResponse, String urlEncodedRelayState,
            byte[] signature, String sigAlgo) throws IOException {
        return getRedirectURLWithSignature(GeneralConstants.SAML_RESPONSE_KEY, urlEncodedResponse, urlEncodedRelayState,
                signature, sigAlgo);
    }

    private static String getRedirectURLWithSignature(String samlParameter, String urlEncoded, String urlEncodedRelayState,
            byte[] signature, String sigAlgo) throws IOException {
        StringBuilder sb = new StringBuilder();

        addParameter(sb, samlParameter, urlEncoded);

        if (isNotNull(urlEncodedRelayState)) {
            addParameter(sb, GeneralConstants.RELAY_STATE, urlEncodedRelayState);
        }

        // SigAlg
        String sigAlg = SignatureUtil.getXMLSignatureAlgorithmURI(sigAlgo);

        sigAlg = URLEncoder.encode(sigAlg, "UTF-8");

        addParameter(sb, GeneralConstants.SAML_SIG_ALG_REQUEST_KEY, sigAlg);

        // Encode the signature value
        String encodedSig = RedirectBindingUtil.base64URLEncode(signature);

        addParameter(sb, GeneralConstants.SAML_SIGNATURE_REQUEST_KEY, encodedSig);

        return sb.toString();
    }

    private static void addParameter(StringBuilder queryString, String paramName, String paramValue) {
        String parameterSeparator = "&";

        if (queryString.length() == 0) {
            parameterSeparator = "";
        }

        queryString.append(parameterSeparator).append(paramName).append("=").append(paramValue);
    }

    private static String getToken(String queryString, String token) {
        if (queryString == null)
            throw logger.nullArgumentError("queryString");

        token += "=";

        int start = queryString.indexOf(token);
        if (start < 0)
            return null;

        int end = queryString.indexOf("&", start);

        if (end == -1)
            return queryString.substring(start);

        return queryString.substring(start, end);
    }

    private static String getTokenValue(String token) {
        if (token == null)
            return token;

        int eq = token.indexOf('=');
        if (eq == -1)
            return token;
        else
            return token.substring(eq + 1);
    }
}