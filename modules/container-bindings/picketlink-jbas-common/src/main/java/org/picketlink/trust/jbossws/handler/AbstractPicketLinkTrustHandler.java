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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.security.SecurityConstants;
import org.jboss.wsf.common.handler.GenericSOAPHandler;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Abstract base class for the PicketLink Trust Handlers</p>
 * <p>This class implements directly the {@link SOAPHandler} interface because the {@link GenericSOAPHandler} package name changes between JBossWS versions.</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 * @since Apr 11, 2011
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPicketLinkTrustHandler<C extends LogicalMessageContext> implements SOAPHandler {
    
    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    protected static Set<QName> headers;

    protected static final String SEC_MGR_LOOKUP = SecurityConstants.JAAS_CONTEXT_ROOT;
    protected static final String AUTHZ_MGR_LOOKUP = "java:comp/env/security/authorizationMgr";

    protected SecurityAdaptorFactory secAdapterfactory;

    private String securityDomainName;

    static {
        HashSet<QName> set = new HashSet<QName>();
        set.add(Constants.WSSE_HEADER_QNAME);
        headers = Collections.unmodifiableSet(set);
    }

    public Set<QName> getHeaders() {
        // return a collection with just the wsse:Security header to pass the MustUnderstand check on it
        return headers;
    }

    /**
     * <p>Utility method to get the {@link ServletContext} from the specified {@link MessageContext}.</p>
     * 
     * @param msgContext
     * @return
     */
    protected ServletContext getServletContext(MessageContext msgContext) {
        return (ServletContext) msgContext.get(MessageContext.SERVLET_CONTEXT);
    }

    /**
     * <p>Returns the security domain name configured for the deployment.</p>
     * 
     * @param msgContext
     * @return
     * @throws ConfigurationException if no security domain is configured.
     */
    protected String getSecurityDomainName(MessageContext msgContext) throws ConfigurationException {
        if (this.securityDomainName == null) {
            InputStream is = null;

            try {
                is = getJBossWeb(getServletContext(msgContext));

                if (is != null) {
                    Document document = DocumentUtil.getDocument(is);
                    securityDomainName = DocumentUtil.getChildElement(document.getDocumentElement(),
                            new javax.xml.namespace.QName("security-domain")).getTextContent();
                }
            } catch (Exception e) {
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        
        if (this.securityDomainName == null) {
            throw logger.securityDomainNotFound();
        }
        
        return this.securityDomainName;
    }

    /**
     * <p>Returns a {@link InputStream} for the jboss-web.xml configuration file.</p>
     * 
     * @param context
     * @return
     */
    private InputStream getJBossWeb(ServletContext context) {
        if (context == null)
            throw logger.nullValueError("Servlet Context");

        return context.getResourceAsStream("/WEB-INF/jboss-web.xml");
    }


    /**
     * Given a {@link Document}, create the WSSE element
     *
     * @param document
     * @return
     */
    protected Element getSecurityHeaderElement(Document document) {
        Element element = document.createElementNS(Constants.WSSE_NS, Constants.WSSE_HEADER);
        Util.addNamespace(element, Constants.WSSE_PREFIX, Constants.WSSE_NS);
        Util.addNamespace(element, Constants.WSU_PREFIX, Constants.WSU_NS);
        Util.addNamespace(element, Constants.XML_ENCRYPTION_PREFIX, Constants.XML_SIGNATURE_NS);
        return element;
    }

    protected void trace(MessageContext msgContext) {
        if (logger.isTraceEnabled()) {
            if (msgContext instanceof SOAPMessageContext) {
                SOAPMessageContext soapMessageContext = (SOAPMessageContext) msgContext;
                logger.trace("WSDL_PORT=" + soapMessageContext.get(SOAPMessageContext.WSDL_PORT));
                logger.trace("WSDL_OPERATION=" + soapMessageContext.get(SOAPMessageContext.WSDL_OPERATION));
                logger.trace("WSDL_INTERFACE=" + soapMessageContext.get(SOAPMessageContext.WSDL_INTERFACE));
                logger.trace("WSDL_SERVICE=" + soapMessageContext.get(SOAPMessageContext.WSDL_SERVICE));
            }
        }
    }

    /**
     * Given the NameID {@link Element}, return the user name
     *
     * @param nameID
     * @return
     */
    protected String getUsername(final Element nameID) {
        String username = nameID.getNodeValue();
        if (username == null) {
            final NodeList childNodes = nameID.getChildNodes();
            final int size = childNodes.getLength();
            for (int i = 0; i < size; i++) {
                final Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    username = childNode.getNodeValue();
                }
            }
        }
        return username;
    }

    /**
     * Get the SAML Assertion from the subject
     *
     * @return
     */
    protected Element getAssertionFromSubject() {
        Element assertion = null;
        Subject subject = SecurityActions.getAuthenticatedSubject();

        if (subject == null) {
            logger.trace("No authentication Subject found, cannot provide any user roles!");
            return assertion;
        }

        Set<Object> creds = subject.getPublicCredentials();
        if (creds != null) {
            for (Object cred : creds) {
                if (cred instanceof SamlCredential) {
                    SamlCredential samlCredential = (SamlCredential) cred;
                    try {
                        assertion = samlCredential.getAssertionAsElement();
                    } catch (ProcessingException e) {
                        logger.samlAssertionPasingFailed(e);
                    }
                    break;
                }
            }
        }
        return assertion;
    }

    protected Object lookupJNDI(String str) {
        try {
            Context context = new InitialContext();
            return context.lookup(str);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Handles the incoming message and decides which method should be called: <code>handleOutbound</code> or <code>handleInbound</code></p>.
     * 
     * @param msgContext
     * @return
     */
    public boolean handleMessage(MessageContext msgContext) {
        Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound == null)
           throw new IllegalStateException("Cannot obtain required property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        return outbound ? handleOutbound(msgContext) : handleInbound(msgContext);
    }

    protected boolean handleOutbound(MessageContext msgContext) {
        return true;
    }

    protected boolean handleInbound(MessageContext msgContext) {
        return true;
    }

    public boolean handleFault(MessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
        
    }
}