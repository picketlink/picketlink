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
package org.picketlink.identity.federation.core.wstrust.handlers;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.STSClientPool;
import org.picketlink.identity.federation.core.wstrust.STSClientFactory;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.picketlink.common.constants.WSTrustConstants.FAILED_AUTHENTICATION;
import static org.picketlink.common.constants.WSTrustConstants.INVALID_SECURITY;
import static org.picketlink.common.constants.WSTrustConstants.SECURITY_TOKEN_UNAVAILABLE;

/**
 * STSSecurityHandler is a server-side JAX-WS SOAP Protocol handler that will extract a Security Token from the SOAP
 * Security
 * Header and validate the token with the configured Security Token Service (STS).
 * <p/>
 *
 * This class is abstract to simpify is usage as the intention is for a handler to be specified in a server side
 * handler
 * chain.
 * Here different Security Header specifications and security token specifications can be specified using class names
 * instead of
 * using properties which would force users to finding and setting the correct namespaces. Hopefully this will be
 * easier
 * and
 * less error prone.
 *
 * <h3>Concrete implementations</h3>
 * Subclasses a required to implement two methods:
 * <ul>
 * <li> {@link #getSecurityElementQName()} This should return the qualified name of the security header. This lets us
 * support
 * different versions.</li>
 *
 * <li>{@link #getTokenElementQName()} This should return the qualified name of the security token element that should
 * exist in
 * the security header. This lets us support different tokens that can be validated with the configured STS.</li>
 * </ul>
 *
 * <h3>Configuration</h3>
 * handlerchain.xml example:
 *
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <jws:handler-config xmlns:jws="http://java.sun.com/xml/ns/javaee">
 *   <jws:handler-chains>
 *     <jws:handler-chain>
 *       <jws:handler>
 *         <jws:handler-class>org.picketlink.identity.federation.core.wstrust.handlers.STSSaml20Handler</jws:handler-class>
 *       </jws:handler>
 *     </jws:handler-chain>
 *   </jws:handler-chains>
 * </jws:handler-config>
 * }
 * </pre>
 * <p/>
 *
 * This class uses {@link STSClient} to interact with an STS. By default the configuration properties are set in a file
 * named
 * {@link STSClientConfig#DEFAULT_CONFIG_FILE}. This can be overridden by specifying environment entries in a
 * deployment
 * descriptor.
 *
 * For example in web.xml:
 *
 * <pre>
 * {@code
 * <env-entry>
 *   <env-entry-name>STSClientConfig</env-entry-name>
 *   <env-entry-type>java.lang.String</env-entry-type>
 *   <env-entry-value>/sts-client.properties</env-entry-value>
 * </env-entry>
 * }
 * </pre>
 *
 * Username and password for the STS can be configured as shown above in the sts-client.properties file. But it may
 * also
 * be
 * specified by a handler earlier in the handlerchain. Such a handler is expected to extract the username and password
 * for the
 * desired location and put these values into the SOAPMessageContext using: <br/>
 * {@link #USERNAME_MSG_CONTEXT_PROPERTY} <br/>
 * {@link #PASSWORD_MSG_CONTEXT_PROPERTY} <br/>
 * These will then be used when contacting the STS, overriding any such values that were parsed from the configuration
 * file.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public abstract class STSSecurityHandler implements SOAPHandler<SOAPMessageContext> {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Constant that can be used by handlers to set the username in the SOAPMessageContext.
     */
    public static final String USERNAME_MSG_CONTEXT_PROPERTY = "org.picketlink.identity.federation.core.wstrust.handlers.username";

    /**
     * Constant that can be used by handlers to set the password in the SOAPMessageContext.
     */
    public static final String PASSWORD_MSG_CONTEXT_PROPERTY = "org.picketlink.identity.federation.core.wstrust.handlers.password";

    /**
     * The path to the jboss-sts-client.properties file.
     */
    private String configFile = STSClientConfig.DEFAULT_CONFIG_FILE;

    /**
     * The STSClient configuration builder.
     */
    private STSClientConfig.Builder configBuilder;

    /**
     * Subclasses can return the QName of the Security header element in usage.
     *
     * @return QName
     */
    public abstract QName getSecurityElementQName();

    /**
     * Subclasses can return the QName of the Security Element that should be used as the token for validation.
     *
     * @return QName
     */
    public abstract QName getTokenElementQName();

    /**
     * Post construct will be called when the handler is deployed.
     *
     * @throws WebServiceException
     */
    @PostConstruct
    public void parseSTSConfig() {
        configBuilder = new STSClientConfig.Builder(configFile);
    }

    /**
     * Will process in-bound messages and extract a security token from the SOAP Header. This token will then be validated using
     * by calling the STS..
     *
     * @param messageContext The {@link SOAPMessageContext messageContext}.
     * @return true If the security token was correctly validated or if this call was an outbound message.
     * @throws WebServiceException If the security token could not be validated.
     */
    public boolean handleMessage(final SOAPMessageContext messageContext) {
        if (isOutBound(messageContext))
            return true;

        STSClient stsClient = null;
        try {
            final Element securityToken = extractSecurityToken(messageContext, getSecurityElementQName(),
                    getTokenElementQName());
            if (securityToken == null) {
                throwSecurityTokenUnavailable();
            }

            setUsernameFromMessageContext(messageContext, configBuilder);
            setPasswordFromMessageContext(messageContext, configBuilder);
            STSClientConfig stsClientConfig = configBuilder.build();
            stsClient = createSTSClient(stsClientConfig);

            if (stsClient.validateToken(securityToken) == false) {
                throwFailedAuthentication();
            }
        } catch (final WSTrustException e) {
            throwInvalidSecurity();
        } catch (ParsingException e) {
            throwInvalidSecurity();
        } finally {
            if (stsClient != null) {
                STSClientPool pool = STSClientFactory.getInstance();
                if (pool != null) {
                    pool.returnClient(stsClient);
                }
            }
        }

        return true;
    }

    @SuppressWarnings({"rawtypes"})
    private Element extractSecurityToken(final SOAPMessageContext messageContext, final QName securityQName,
                                         final QName tokenQName) {
        try {
            if (securityQName == null)
                throw logger.nullArgumentError("securityQName from subclass");
            if (tokenQName == null)
                throw logger.nullArgumentError("tokenQName from subclass");

            final SOAPHeader soapHeader = messageContext.getMessage().getSOAPHeader();
            final Iterator securityHeaders = soapHeader.getChildElements(securityQName);
            while (securityHeaders.hasNext()) {
                final SOAPHeaderElement elem = (SOAPHeaderElement) securityHeaders.next();
                // Check if the header is equal to the one this Handler is configured for.
                if (elem.getElementQName().equals(securityQName)) {
                    final Iterator childElements = elem.getChildElements(tokenQName);
                    while (childElements.hasNext()) {
                        return (Element) childElements.next();
                    }
                }
            }
        } catch (final SOAPException e) {
            throwInvalidSecurity();
        }
        return null;
    }

    private void throwSecurityTokenUnavailable() throws SOAPFaultException {
        SOAPFault soapFault = createSoapFault(ErrorCodes.NULL_VALUE + "No security token could be found in the SOAP Header",
                SECURITY_TOKEN_UNAVAILABLE);
        throw new SOAPFaultException(soapFault);
    }

    private void throwFailedAuthentication() throws SOAPFaultException {
        SOAPFault soapFault = createSoapFault("The security token could not be authenticated or authorized",
                FAILED_AUTHENTICATION);
        throw new SOAPFaultException(soapFault);
    }

    private void throwInvalidSecurity() throws SOAPFaultException {
        SOAPFault soapFault = createSoapFault("An error occurred while processing the security header", INVALID_SECURITY);
        throw new SOAPFaultException(soapFault);
    }

    private SOAPFault createSoapFault(final String msg, final QName qname) {
        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            return soapFactory.createFault(msg, qname);
        } catch (SOAPException e) {
            throw new WebServiceException("Exception while trying to create SOAPFault", e);
        }
    }

    /**
     * If a property was set for the key {@link #USERNAME_MSG_CONTEXT_PROPERTY} it will be retrieved by this method and set on
     * the passed-in builder instance.
     *
     * @param context The SOAPMessageContext which might contain a username property.
     * @param builder The STSClientConfigBuilder which be updated if the SOAPMessageContext contains the username property.
     */
    private void setUsernameFromMessageContext(final SOAPMessageContext context, final STSClientConfig.Builder builder) {
        final String username = (String) context.get(USERNAME_MSG_CONTEXT_PROPERTY);
        if (username != null)
            configBuilder.username(username);
    }

    /**
     * If a property was set for the key {@link #PASSWORD_MSG_CONTEXT_PROPERTY} it will be retrieved by this method and set on
     * the passed-in builder instance.
     *
     * @param context The SOAPMessageContext which might contain a password property.
     * @param builder The STSClientConfigBuilder which be updated if the SOAPMessageContext contains the password property.
     */
    private void setPasswordFromMessageContext(final SOAPMessageContext context, final STSClientConfig.Builder builder) {
        final String password = (String) context.get(PASSWORD_MSG_CONTEXT_PROPERTY);
        if (password != null)
            configBuilder.password(password);
    }

    public Set<QName> getHeaders() {
        return Collections.singleton(getSecurityElementQName());
    }

    public boolean handleFault(final SOAPMessageContext messageContext) {
        return true;
    }

    public void close(final MessageContext messageContext) {
        // NoOp.
    }

    /**
     * This setter enables the injection of the jboss-sts-client.properties file path.
     *
     * @param configFile
     */
    @Resource(name = "STSClientConfig")
    public void setConfigFile(final String configFile) {
        if (configFile != null) {
            this.configFile = configFile;
        }
    }

    STSClientConfig.Builder getConfigBuilder() {
        return configBuilder;
    }

    STSClient createSTSClient(final STSClientConfig config) throws ParsingException {
        STSClientPool pool = STSClientFactory.getInstance();
        if (pool.configExists(config) == false) {
            pool.createPool(config);
        }
        return pool.getClient(config);
    }

    private boolean isOutBound(final SOAPMessageContext messageContext) {
        return ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
    }
}