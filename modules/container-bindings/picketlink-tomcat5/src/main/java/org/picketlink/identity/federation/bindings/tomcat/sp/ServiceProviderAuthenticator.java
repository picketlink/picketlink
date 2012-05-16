package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.catalina.connector.Response;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.w3c.dom.Document;

/**
 * Unified Service Provider Authenticator
 *
 * @author anil saldhana
 */
public class ServiceProviderAuthenticator extends AbstractSPFormAuthenticator {

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.AbstractSPFormAuthenticator#sendRequestToIDP(java.lang.String,
     * org.w3c.dom.Document, java.lang.String, org.apache.catalina.connector.Response, boolean)
     */
    @Override
    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest, String destinationQueryStringWithSignature) throws ProcessingException, ConfigurationException, IOException {
        if (isHttpPostBinding()) {
            sendHttpPostBindingRequest(destination, samlDocument, relayState, response, willSendRequest);
        } else {
            sendHttpRedirectRequest(destination, samlDocument, relayState, response, willSendRequest, destinationQueryStringWithSignature);
        }
    }

    /**
     * <p>
     * Sends a HTTP Redirect request to the IDP.
     * </p>
     *
     * @param destination
     * @param relayState
     * @param response
     * @param willSendRequest
     * @param destinationQueryStringWithSignature
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    private void sendHttpRedirectRequest(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest, String destinationQueryStringWithSignature) throws IOException,
            ProcessingException, ConfigurationException {
        String destinationQueryString = null;

        // We already have queryString with signature from SAML2SignatureGenerationHandler
        if (destinationQueryStringWithSignature != null) {
            destinationQueryString = destinationQueryStringWithSignature;
        }
        else {
            String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));
            destinationQueryString = RedirectBindingUtil.getDestinationQueryString(base64Request, relayState, willSendRequest);
        }

        RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();

        holder.setDestination(destination).setDestinationQueryString(destinationQueryString);

        HTTPRedirectUtil.sendRedirectForRequestor(RedirectBindingUtil.getDestinationURL(holder), response);
    }

    /**
     * <p>
     * Sends a HTTP POST request to the IDP.
     * </p>
     *
     * @param destination
     * @param samlDocument
     * @param relayState
     * @param response
     * @param willSendRequest
     * @throws TrustKeyProcessingException
     * @throws ProcessingException
     * @throws IOException
     * @throws ConfigurationException
     */
    private void sendHttpPostBindingRequest(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest) throws ProcessingException, IOException,
            ConfigurationException {
        String samlMessage = PostBindingUtil.base64Encode(DocumentUtil.getDocumentAsString(samlDocument));

        DestinationInfoHolder destinationHolder = new DestinationInfoHolder(destination, samlMessage, relayState);

        PostBindingUtil.sendPost(destinationHolder, response, willSendRequest);
    }
}