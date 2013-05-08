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

import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.callbacks.SecurityContextCallbackHandler;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.trust.jbossws.util.JBossWSNativeStackUtil;
import org.picketlink.trust.jbossws.util.JBossWSSERoleExtractor;
import org.w3c.dom.Node;

/**
 * <p>Base class for authorization handlers for POJO Web services based on the Authorize Operation on the JBossWS Native stack</p>
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author Anil.Saldhana@redhat.com
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 * @since Apr 11, 2011
 */
public abstract class AbstractWSAuthorizationHandler extends AbstractPicketLinkTrustHandler {
    public static final String UNCHECKED = "unchecked";

    // A simple hashmap that reduces the reparsing of jboss-wsse.xml for the same keys
    protected Map<String, List<String>> cache = new HashMap<String, List<String>>();

    @Override
    protected boolean handleInbound(MessageContext msgContext) {

        logger.trace("Handling Inbound Message");
        
        trace(msgContext);
        
        ServletContext context = getServletContext(msgContext);
        // Read the jboss-wsse.xml file
        InputStream is = getWSSE(context);
        if (is == null)
            throw logger.jbossWSUnableToLoadJBossWSSEConfigError();

        QName portName = (QName) msgContext.get(MessageContext.WSDL_PORT);
        QName opName = (QName) msgContext.get(MessageContext.WSDL_OPERATION);

        if (portName == null)
            portName = JBossWSNativeStackUtil.getPortNameViaReflection(getClass(), msgContext);

        if (portName == null)
            throw logger.nullValueError("port name from the message context");

        if (opName == null)
            opName = getOperationName(msgContext);

        if (opName == null)
            throw logger.nullValueError("operation name from the message context");

        List<String> roles = null;

        String key = portName.getLocalPart() + "_" + opName.toString();

        // First check in cache
        if (cache.containsKey(key)) {
            roles = cache.get(key);
        } else {
            try {
                roles = JBossWSSERoleExtractor.getRoles(is, portName.getLocalPart(), opName.toString());
            } catch (ProcessingException e) {
                throw new RuntimeException(e);
            }
            cache.put(key, roles);
        }

        if (!roles.contains(UNCHECKED)) {
            AuthorizationManager authorizationManager = null;
            
            try {
                authorizationManager = getAuthorizationManager(msgContext);
            } catch (ConfigurationException e) {
                logger.authorizationManagerError(e);
                throw new RuntimeException(e);
            }
 
            Subject subject = SecurityActions.getAuthenticatedSubject();
            
            Set<Principal> expectedRoles = rolesSet(roles);
            if (!authorizationManager.doesUserHaveRole(null, expectedRoles)) {
                SecurityContext sc = SecurityActions.getSecurityContext();
                StringBuilder builder = new StringBuilder("Authorization Failed:Subject=");
                builder.append(subject).append(":Expected Roles=").append(expectedRoles);
                SecurityContextCallbackHandler scbh = new SecurityContextCallbackHandler(sc);
                builder.append("::Actual Roles=").append(authorizationManager.getSubjectRoles(subject, scbh));
                logger.error(builder.toString());

                throw logger.jbossWSAuthorizationFailed();
            }
        }
        return true;
    }

    protected Set<Principal> rolesSet(List<String> roles) {
        Set<Principal> principals = new HashSet<Principal>();
        for (String role : roles) {
            principals.add(new SimplePrincipal(role));
        }
        return principals;
    }

    protected InputStream getWSSE(ServletContext context) {
        if (context == null)
            throw logger.nullValueError("Servlet Context");

        InputStream is = context.getResourceAsStream("/WEB-INF/jboss-wsse.xml");
        return is;
    }

    protected InputStream load(ClassLoader cl) {
        InputStream is = null;
        is = cl.getResourceAsStream("WEB-INF/jboss-wsse.xml");
        if (is == null)
            is = cl.getResourceAsStream("/WEB-INF/jboss-wsse.xml");
        return is;
    }

    private QName getOperationName(MessageContext msgContext) {
        SOAPMessageContext soapMessageContext = (SOAPMessageContext) msgContext;
        SOAPMessage soapMessage = soapMessageContext.getMessage();
        SOAPBody soapBody;
        try {
            soapBody = soapMessage.getSOAPBody();
            Node child = soapBody.getFirstChild();
            String childNamespace = child.getNamespaceURI();
            String childName = child.getLocalName();
            return new QName(childNamespace, childName);
        } catch (SOAPException e) {
            logger.jbossWSErrorGettingOperationName(e);
        }
        return null;
    }
    
    /**
     * <p>Returns the {@link AuthorizationManager} associated with the application's security domain. </p>
     * 
     * @param msgContext
     * @return
     * @throws ConfigurationException
     */
    protected AuthorizationManager getAuthorizationManager(MessageContext msgContext) throws ConfigurationException {
        return (AuthorizationManager) lookupJNDI(SecurityConstants.JAAS_CONTEXT_ROOT + getSecurityDomainName(msgContext) + "/authorizationMgr");
    }
}