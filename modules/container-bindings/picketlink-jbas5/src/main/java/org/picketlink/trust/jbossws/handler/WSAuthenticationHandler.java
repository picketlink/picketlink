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

import javax.xml.ws.handler.MessageContext;

import org.jboss.security.AuthenticationManager;

/**
 * Perform Authentication for POJO Web Services
 * 
 * Based on the Authorize Operation on the JBossWS Native stack
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class WSAuthenticationHandler extends AbstractWSAuthenticationHandler {

    @Override
    protected AuthenticationManager getAuthenticationManager(MessageContext msgContext) {
        return (AuthenticationManager) lookupJNDI("java:comp/env/security/securityMgr");
    }
    
}