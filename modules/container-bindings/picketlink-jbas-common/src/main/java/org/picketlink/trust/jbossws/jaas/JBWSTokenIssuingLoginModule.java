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
package org.picketlink.trust.jbossws.jaas;

import java.net.URI;
import java.security.Principal;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.login.LoginException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.Handler;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.PicketLinkDispatch;
import org.picketlink.trust.jbossws.handler.BinaryTokenHandler;
import org.picketlink.trust.jbossws.handler.MapBasedTokenHandler;
import org.w3c.dom.Element;

/**
 * A subclass of {@link STSIssuingLoginModule} that adds in JBoss WS specific details
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 22, 2011
 */
@SuppressWarnings("restriction")
public class JBWSTokenIssuingLoginModule extends STSIssuingLoginModule {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Key in the options to customize the WS-Addressing Issuer in the WS-T Call
     */
    public static final String WSA_ISSUER = "wsaIssuer";

    /**
     * Key in the options to customize the WS-Policy Applies To in the WS-T Call
     */
    public static final String WSP_APPIESTO = "wspAppliesTo";

    @Override
    protected Builder createBuilder() {
        Builder builder = super.createBuilder();
        builder.wsaIssuer((String) options.get(WSA_ISSUER));
        builder.wspAppliesTo((String) options.get(WSP_APPIESTO));
        return builder;
    }

    @Override
    protected STSClient createWSTrustClient(STSClientConfig config) {
        
        String binaryTokenKey = (String) options.get(MapBasedTokenHandler.SYS_PROP_TOKEN_KEY);
        if (binaryTokenKey == null) {
            binaryTokenKey = SecurityActions.getSystemProperty(MapBasedTokenHandler.SYS_PROP_TOKEN_KEY, 
                    MapBasedTokenHandler.DEFAULT_TOKEN_KEY);
        }
        Object binaryToken = sharedState.get(binaryTokenKey);

        Map<String, ? super Object> STSClientOptions = new HashMap<String, Object> (options);
        if (binaryToken != null) {
            STSClientOptions.put(binaryTokenKey, binaryToken);
        }
        
        return new JBWSTokenClient(config, STSClientOptions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean commit() throws LoginException {
        boolean result = super.commit();
        if (result) {
            SamlCredential samlCredential = null;
            Set<Object> creds = subject.getPublicCredentials();
            for (Object cred : creds) {
                if (cred instanceof SamlCredential) {
                    samlCredential = (SamlCredential) cred;
                    break;
                }
            }
            if (samlCredential == null)
                throw logger.authSAMLCredentialNotAvailable();
            Principal principal = new PicketLinkPrincipal("");
            if (super.isUseFirstPass()) {
                this.sharedState.put("javax.security.auth.login.name", principal);
                super.sharedState.put("javax.security.auth.login.password", samlCredential);
            }

        }
        return result;
    }

    public class JBWSTokenClient extends STSClient {

        /**
         * Indicates request type, could be either {@link WSTrustConstants.ISSUE_REQUEST} or {@link WSTrustConstants.VALIDATE_REQUEST}.
         */
        private String requestType = WSTrustConstants.ISSUE_REQUEST;
        
        private DatatypeFactory dataTypefactory;

        public JBWSTokenClient() {
            super();

            try {
                this.dataTypefactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException dce) {
                throw logger.wsTrustUnableToGetDataTypeFactory(dce);
            }
        }

        public JBWSTokenClient(STSClientConfig config) {
            super(config);
            requestType = config.getRequestType();
            
            try {
                this.dataTypefactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException dce) {
                throw logger.wsTrustUnableToGetDataTypeFactory(dce);
            }
        }
        

        @SuppressWarnings("rawtypes")
        public JBWSTokenClient(STSClientConfig config, Map<String, ? super Object> options) {
            super(config);
        
            try {
                this.dataTypefactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException dce) {
                throw logger.wsTrustUnableToGetDataTypeFactory(dce);
            }
            
            requestType = (String) options.get(STSClientConfig.REQUEST_TYPE);
            if (requestType == null) {
                requestType = config.getRequestType();
            }

            String soapBinding = (String) options.get(STSClientConfig.SOAP_BINDING);
            if (soapBinding != null) {
                setSoapBinding(soapBinding);
            }
            
            
            // Get pre-constructed Dispatch from super
            Dispatch<Source> dispatch = super.getDispatch();

            String overrideDispatchStr = (String) options.get("overrideDispatch");
            if (StringUtil.isNotNull(overrideDispatchStr)) {
                boolean bool = Boolean.valueOf(overrideDispatchStr);
                if (bool) {
                    dispatch = new PicketLinkDispatch(dispatch, (String) options.get("endpointAddress"));
                    String useWSSE = (String) options.get("useWSSE");
                    if (StringUtil.isNotNull(useWSSE) && useWSSE.equalsIgnoreCase("true")) {
                        ((PicketLinkDispatch) dispatch).setUseWSSE(true);
                    }
                }
            }

            Binding binding = dispatch.getBinding();

            List<Handler> handlers = binding.getHandlerChain();

            String handlerStr = (String) options.get("handlerChain");
            
            if (StringUtil.isNotNull(handlerStr)) {
                List<String> tokens = StringUtil.tokenize(handlerStr);
                for (String token : tokens) {
                    if (token.equalsIgnoreCase("binary")) {
                        BinaryTokenHandler binaryTokenHandler = new BinaryTokenHandler();
                        handlers.add(binaryTokenHandler);
                    } else if (token.equalsIgnoreCase("map")) {
                        MapBasedTokenHandler mapBasedHandler = new MapBasedTokenHandler(
                                options);
                        handlers.add(mapBasedHandler);
                    } else {
                        String className = (token.equalsIgnoreCase("saml2") ? "org.picketlink.trust.jbossws.handler.SAML2Handler"
                                : token);
                        ClassLoader cl = SecurityActions
                                .getClassLoader(getClass());
                        try {
                            handlers.add((Handler) cl.loadClass(className)
                                    .newInstance());
                        } catch (Exception e) {
                            throw logger.authUnableToInstantiateHandler(token,
                                    e);
                        }
                    }
                }
            }

            binding.setHandlerChain(handlers);

            setDispatch(dispatch);

            String securityDomainForFactory = (String) options.get("securityDomainForFactory");
            if (StringUtil.isNotNull(securityDomainForFactory)) {
                logger.trace("We got security domain for domain ssl factory = " + securityDomainForFactory);
                logger.trace("Setting it on the system property org.jboss.security.ssl.domain.name");

                String sslFactoryName = "org.jboss.security.ssl.JaasSecurityDomainSocketFactory";
                SecurityActions.setSystemProperty("org.jboss.security.ssl.domain.name", securityDomainForFactory);
                // StubExt.PROPERTY_SOCKET_FACTORY
                dispatch.getRequestContext().put("org.jboss.ws.socketFactory", sslFactoryName);

                // If we are using PL Dispatch. Then we need to set the SSL Socket Factory
                if (dispatch instanceof PicketLinkDispatch) {
                    ClassLoader cl = SecurityActions.getClassLoader(getClass());
                    SSLSocketFactory socketFactory = null;
                    if (cl != null) {
                        try {
                            Class<?> clazz = cl.loadClass(sslFactoryName);
                            socketFactory = (SSLSocketFactory) clazz.newInstance();
                        } catch (Exception e) {
                            cl = SecurityActions.getContextClassLoader();
                            try {
                                Class<?> clazz = cl.loadClass(sslFactoryName);
                                socketFactory = (SSLSocketFactory) clazz.newInstance();
                            } catch (Exception e1) {
                                throw logger.jbossWSUnableToCreateSSLSocketFactory(e1);
                            }
                        } finally {
                            if (socketFactory != null) {
                                ((PicketLinkDispatch) dispatch).setSSLSocketFactory(socketFactory);
                            } else
                                throw logger.jbossWSUnableToFindSSLSocketFactory();
                        }
                    } else {
                        logger.trace("Classloader is null. Unable to set the SSLSocketFactory on PicketLinkDispatch");
                    }
                }
            }
        }
        
        @Override
        public Element issueToken(RequestSecurityToken request)
                throws WSTrustException {
            
            if (requestType.equals(WSTrustConstants.VALIDATE_REQUEST)) {
                request.setRequestType(URI.create(requestType));
                ValidateTargetType validateTarget = new ValidateTargetType();

                try {
                    String sUserName = JBWSTokenIssuingLoginModule.this.getSharedUsername();
                    char[] cPassword = JBWSTokenIssuingLoginModule.this.getSharedPassword();
                    Element wsseUsernameToken = createUsernameToken(sUserName, 
                            (cPassword != null ? new String(cPassword) : null)); 
                    validateTarget.add(wsseUsernameToken);
                    request.setValidateTarget(validateTarget);
                }
                catch (SOAPException e) {
                    throw new WSTrustException(e);
                }
            }            
            
            return super.issueToken(request);
            
        }

        private Element createUsernameToken(String usernameValue, String passwordValue) throws SOAPException {
            
            QName usernameTokenName = new QName(Constants.WSSE_NS, Constants.WSSE_USERNAME_TOKEN, Constants.WSSE_PREFIX);
            QName usernameName = new QName(Constants.WSSE_NS, Constants.WSSE_USERNAME, Constants.WSSE_PREFIX);
            QName passwordName = new QName(Constants.WSSE_NS, Constants.WSSE_PASSWORD, Constants.WSSE_PREFIX);
            QName createdName = new QName(Constants.WSU_NS, "Created", Constants.WSU_PREFIX);
            
            SOAPFactory factory = SOAPFactory.newInstance();
            SOAPElement usernametoken = factory.createElement(usernameTokenName);
            usernametoken.addNamespaceDeclaration(Constants.WSSE_PREFIX, Constants.WSSE_NS);
            usernametoken.addNamespaceDeclaration(Constants.WSU_PREFIX, Constants.WSU_NS);
            SOAPElement username = factory.createElement(usernameName);
            username.addTextNode(usernameValue);
            SOAPElement password = factory.createElement(passwordName);
            password.addAttribute(new QName("Type"), Constants.PASSWORD_TEXT_TYPE);
            password.addTextNode(passwordValue);

            SOAPElement created = factory.createElement(createdName);
            XMLGregorianCalendar createdCal = dataTypefactory.newXMLGregorianCalendar(new GregorianCalendar()).normalize();
            created.addTextNode(createdCal.toXMLFormat());
            
            usernametoken.addChildElement(username);
            usernametoken.addChildElement(password);
            usernametoken.addChildElement(created);
            return usernametoken;
        }

    }
}