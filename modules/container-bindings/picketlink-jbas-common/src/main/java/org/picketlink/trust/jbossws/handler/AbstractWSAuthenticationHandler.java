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
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.xml.ws.handler.MessageContext;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.identity.Identity;
import org.jboss.security.identity.extensions.CredentialIdentity;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;

/**
 * <p>Base class to perform Authentication for POJO Web Services based on the Authorize Operation on the JBossWS Native stack.</p>
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author Anil.Saldhana@redhat.com
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 * @since Apr 11, 2011
 */
public abstract class AbstractWSAuthenticationHandler extends AbstractPicketLinkTrustHandler {
    
    /* (non-Javadoc)
     * @see org.picketlink.trust.jbossws.handler.AbstractPicketLinkTrustHandler#handleInbound(javax.xml.ws.handler.MessageContext)
     */
    @Override
    protected boolean handleInbound(MessageContext msgContext) {

        logger.trace("Handling Inbound Message");

        trace(msgContext);

        AuthenticationManager authenticationManager = null;
        
        try {
            authenticationManager = getAuthenticationManager(msgContext);
        } catch (ConfigurationException e) {
            logger.authenticationManagerError(e);
            throw new RuntimeException(e);
        }
        
        Principal principal = null;
        Object credential = null;
        Iterator<Identity> iterator = SecurityContextAssociation.getSecurityContext().getSubjectInfo().getIdentities()
                .iterator();

        while (iterator.hasNext()) {
            CredentialIdentity identity = (CredentialIdentity) iterator.next();

            principal = identity.asPrincipal();
            credential = identity.getCredential();
        }

        Subject subject = new Subject();

        if (authenticationManager.isValid(principal, credential, subject) == false) {
            String msg = ErrorCodes.PROCESSING_EXCEPTION + "Authentication failed, principal=" + principal;
            logger.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RuntimeException(e);
        }

        logger.trace("Successfully Authenticated:Principal = " + principal + "  ::subject = " + subject);

        SecurityContext sc = SecurityActions.createSecurityContext(principal, credential, subject);
        SecurityActions.setSecurityContext(sc);

        return true;
    }

    /**
     * <p>Returns the {@link AuthenticationManager} associated with the application's security domain.</p>
     * 
     * @param msgContext
     * @return
     * @throws ConfigurationException
     */
    protected AuthenticationManager getAuthenticationManager(MessageContext msgContext) throws ConfigurationException {
        String securityDomainName = getSecurityDomainName(msgContext);
        
        return (AuthenticationManager) lookupJNDI(SecurityConstants.JAAS_CONTEXT_ROOT + securityDomainName);
    }

}