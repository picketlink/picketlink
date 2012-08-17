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

import java.security.Principal;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.trust.jbossws.Util;
import org.picketlink.trust.jbossws.Constants;

/**
 * 
 * @author pskopek@redhat.com
 *
 */
public class SamlRequestSecurityTokenHandler extends AbstractPicketLinkTrustHandler {
    
    private SOAPFactory factory = null;
    
    @Override
    protected boolean handleInbound(MessageContext msgContext) {
        
        String username = getUserPrincipalName(msgContext);

        SOAPMessage sm = ((SOAPMessageContext) msgContext).getMessage();
        SOAPEnvelope envelope;
        try {
            envelope = sm.getSOAPPart().getEnvelope();
            SOAPBodyElement rst = (SOAPBodyElement) Util.findElement(envelope, new QName(WSTrustConstants.BASE_NAMESPACE, WSTrustConstants.RST));
            if (rst != null) {
                rst.addChildElement(createUsernameToken(username));
            }
        } catch (SOAPException e) {
            logger.jbossWSUnableToCreateBinaryToken(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("SOAPMessage(SamlRequestSecurityTokenHandler)=" + SOAPUtil.soapMessageAsString(sm));            
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
    private SOAPElement createUsernameToken(String usernamevalue) throws SOAPException {
        if (factory == null)
            factory = SOAPFactory.newInstance();
        SOAPElement usernametoken = factory.createElement(Constants.WSSE_USERNAME_TOKEN,
                Constants.WSSE_PREFIX, Constants.WSSE_NS);
        SOAPElement username = factory.createElement(Constants.WSSE_USERNAME, Constants.WSSE_PREFIX,
                Constants.WSSE_NS);
        username.addTextNode(usernamevalue);

        usernametoken.addChildElement(username);
        return usernametoken;
    }
    
    
    /**
     * Get the {@link HttpServletRequest} from the {@link MessageContext}
     *
     * @param msgContext
     * @return
     */
    private HttpServletRequest getHttpRequest(MessageContext msgContext) {
        HttpServletRequest request = (HttpServletRequest) msgContext.get(MessageContext.SERVLET_REQUEST);
        if (request == null) {
            try {
                request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
            } catch (PolicyContextException e) {
                return null;
            }
        }
        return request;
    }
    
    protected String getUserPrincipalName(MessageContext msgContext) {
        HttpServletRequest servletRequest = getHttpRequest(msgContext);
        if (servletRequest == null) {
            logger.warn("Cannot get HttpRequest, ignoring " + SamlRequestSecurityTokenHandler.class.getName());
            return null;
        }
        
        Principal principal = servletRequest.getUserPrincipal();
        if (principal == null) {
            logger.warn("Cannot get Principal, ignoring " + SamlRequestSecurityTokenHandler.class.getName());
            return null;
        }
        
        return principal.getName();
    }
    
}
