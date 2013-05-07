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
package org.picketlink.trust.jbossws.handler;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.Util;

/**
 * <p>
 * Handler that looks for a binary token data that exists in jaasOptionsMap supplied in constructor.
 * </p>
 * <p>
 * <b>Configuration:</b>
 * <p>
 * <i>System Properties:</i>
 * <ul>
 * <li>map.token.key: key which will be used to fetch binary token from the jaasOptionsMap. Default value is ClientID</li>
 * <li>map.token.validation.class.key: validation class for binary token inside handleInbound method</li>
 * <li>binary.http.encodingType: attribute value of the EncodingType attribute</li>
 * <li>binary.http.valueType: attribute value of the ValueType attribute</li>
 * <li>binary.http.valueType.namespace: namespace for the ValueType attribute</li>
 * <li>binary.http.valueType.prefix: namespace for the ValueType attribute</li>
 * </ul>
 * <i>Setters:</i>
 * <p>
 * Please see the see also section.
 * </p>
 * 
 * @see #setEncodingType(String)
 * @see #setValueType(String)
 * @see #setValueTypeNamespace(String)
 * @see #setValueTypePrefix(String) </p> </p>
 * @author Anil.Saldhana@redhat.com
 * @author pskopek@redhat.com
 * @since Jun 11, 2012
 */
public class MapBasedTokenHandler extends AbstractPicketLinkTrustHandler {

    public static final String SYS_PROP_TOKEN_KEY = "map.token.key";
    public static final String DEFAULT_TOKEN_KEY = "ClientID";

    private boolean trace = logger.isTraceEnabled();
    
    /**
     * The JAAS shared options map key name for binary token to be stored in.
     */
    public final String tokenOptionKey = SecurityActions.getSystemProperty(
            SYS_PROP_TOKEN_KEY, "ClientID");

    /**
     * Key in the JAAS options map to find class name to validate token in
     * inbound message handle method.
     */
    public final String validationTokenClassKey = SecurityActions
            .getSystemProperty("map.token.validation.class.key",
                    "tokenValidationClass");

    /**
     * Attribute value for the EncodingType attribute
     */
    private String encodingType = SecurityActions
            .getSystemProperty(
                    "binary.http.encodingType",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");

    /**
     * Attribute value for the ValueType attribute
     */
    private String valueType = SecurityActions.getSystemProperty(
            "binary.http.valueType", null);

    /**
     * Namespace for the ValueType. Can be null. If null, then a separate
     * namespace is not added.
     */
    private String valueTypeNamespace = SecurityActions.getSystemProperty(
            "binary.http.valueType.namespace", null);

    /**
     * Prefix for the ValueType. Can be null.
     */
    private String valueTypePrefix = SecurityActions.getSystemProperty(
            "binary.http.valueType.prefix", null);

    private SOAPFactory factory = null;

    /**
     * Shared options from calling login module (@see
     * 
     * @JBWSTokenIssuingLoginModule).
     */
    private Map<String, ?> jaasLoginModuleOptions = null;

    public MapBasedTokenHandler(Map<String, ?> jaasOptionsMap) {
        jaasLoginModuleOptions = jaasOptionsMap;
    }

    /**
     * <p>
     * Set the EncodingType value.
     * </p>
     * <p>
     * Alternatively, set the system property "binary.http.encodingType"
     * </p>
     * 
     * @param binaryEncodingType
     */
    public void setEncodingType(String binaryEncodingType) {
        this.encodingType = binaryEncodingType;
    }

    /**
     * <p>
     * Set the Value type
     * </p>
     * <p>
     * Alternatively, set the system property "binary.http.valueType"
     * </p>
     * 
     * @param binaryValueType
     */
    public void setValueType(String binaryValueType) {
        this.valueType = binaryValueType;
    }

    /**
     * <p>
     * Set the ValueType Namespace
     * </p>
     * <p>
     * Alternatively, set the system property "binary.http.valueType.namespace"
     * </p>
     * 
     * @param binaryValueNamespace
     */
    public void setValueTypeNamespace(String binaryValueNamespace) {
        this.valueTypeNamespace = binaryValueNamespace;
    }

    /**
     * <p>
     * Set the Value Type Prefix
     * </p>
     * <p>
     * Alternatively, set the system property "binary.http.valueType.prefix"
     * </p>
     * 
     * @param binaryValuePrefix
     */
    public void setValueTypePrefix(String binaryValuePrefix) {
        this.valueTypePrefix = binaryValuePrefix;
    }

    @Override
    protected boolean handleInbound(MessageContext msgContext) {
        if (trace) {
            logger.trace("Handling Inbound Message");
        }

        String tokenValidation = (String) jaasLoginModuleOptions
                .get(validationTokenClassKey);
        if (tokenValidation == null) {
            return true;
        }

        BinaryTokenValidation validation = null;
        try {
            ClassLoader cl = SecurityActions.getClassLoader(getClass());
            validation = (BinaryTokenValidation) cl.loadClass(tokenValidation)
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED
                    + "Class not loaded:" + tokenValidation, e);
        }
        String token = getToken(msgContext);
        if (trace) {
            logger.trace("Validating token=" + token);
        }

        return validation.validateBinaryToken(token, msgContext);
    }

    @Override
    protected boolean handleOutbound(MessageContext msgContext) {
        if (trace) {
            logger.trace("Handling Outbound Message");
        }

        String token = (String) jaasLoginModuleOptions.get(tokenOptionKey);
        if (token == null)
            throw new RuntimeException(ErrorCodes.INJECTED_VALUE_MISSING
                    + tokenOptionKey
                    + " has to be set by calling LoginMoule in option map.");

        SOAPElement security = null;
        try {
            security = create(token);
        } catch (SOAPException e) {
            logger.jbossWSUnableToCreateBinaryToken(e);
        }
        if (security == null) {
            logger.jbossWSUnableToCreateSecurityToken();
            return true;
        }
        SOAPMessage sm = ((SOAPMessageContext) msgContext).getMessage();
        SOAPEnvelope envelope;
        try {
            envelope = sm.getSOAPPart().getEnvelope();
            SOAPHeader header = (SOAPHeader) Util.findElement(envelope,
                    new QName(envelope.getNamespaceURI(), "Header"));
            if (header == null) {
                header = (SOAPHeader) envelope.getOwnerDocument()
                        .createElementNS(envelope.getNamespaceURI(),
                                envelope.getPrefix() + ":Header");
                envelope.insertBefore(header, envelope.getFirstChild());
            }
            header.addChildElement(security);
        } catch (SOAPException e) {
            logger.jbossWSUnableToCreateBinaryToken(e);
        }
        if (trace) {
            logger.trace("SOAP Message=" + SOAPUtil.soapMessageAsString(sm));
        }
        return true;
    }

    /**
     * Given a binary token, create a {@link SOAPElement}
     * 
     * @param token
     * @return
     * @throws SOAPException
     */
    private SOAPElement create(String token) throws SOAPException {
        if (factory == null)
            factory = SOAPFactory.newInstance();
        SOAPElement security = factory.createElement(Constants.WSSE_LOCAL,
                Constants.WSSE_PREFIX, Constants.WSSE_NS);

        if (valueTypeNamespace != null) {
            security.addNamespaceDeclaration(valueTypePrefix,
                    valueTypeNamespace);
        }

        SOAPElement binarySecurityToken = factory.createElement(
                Constants.WSSE_BINARY_SECURITY_TOKEN, Constants.WSSE_PREFIX,
                Constants.WSSE_NS);
        binarySecurityToken.addTextNode(token);
        if (valueType != null && !valueType.isEmpty()) {
            binarySecurityToken.setAttribute(Constants.WSSE_VALUE_TYPE,
                    valueType);
        }
        if (encodingType != null) {
            binarySecurityToken.setAttribute(Constants.WSSE_ENCODING_TYPE,
                    encodingType);
        }

        security.addChildElement(binarySecurityToken);
        return security;
    }

    private String getToken(MessageContext msgContext) {

        SOAPMessage sm = ((SOAPMessageContext) msgContext).getMessage();
        SOAPEnvelope envelope;
        try {
            envelope = sm.getSOAPPart().getEnvelope();
            SOAPHeader header = (SOAPHeader) Util.findElement(envelope,
                    new QName(envelope.getNamespaceURI(), "Header"));

            if (header == null) {
                header = (SOAPHeader) envelope.getOwnerDocument()
                        .createElementNS(envelope.getNamespaceURI(),
                                envelope.getPrefix() + ":Header");
            }
            return Util.findElementByWsuId(header, "BinarySecurityToken")
                    .getTextContent();

        } catch (SOAPException e) {
            logger.jbossWSUnableToCreateBinaryToken(e);
            return null;
        }
    }

}