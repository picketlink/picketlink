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
package org.picketlink.identity.federation.bindings.tomcat;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;

/**
 * An authenticator that delegates actual authentication to a realm, and in turn to a security manager, by presenting a
 * "conventional" identity. The security manager must accept the conventional identity and generate the real identity for the
 * authenticated principal.
 *
 * @author <a href="mailto:ovidiu@novaordis.com">Ovidiu Feodorov</a>
 * @author Anil.Saldhana@redhat.com
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @since Apr 11, 2011
 */
public class PicketLinkAuthenticator extends AbstractPicketLinkAuthenticator {

    /* (non-Javadoc)
     * @see org.apache.catalina.authenticator.AuthenticatorBase#authenticate(org.apache.catalina.connector.Request, javax.servlet.http.HttpServletResponse, org.apache.catalina.deploy.LoginConfig)
     */
    @Override
    public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws IOException {
        return super.performAuthentication(request, (Response) response, config);
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.bindings.tomcat.AbstractPicketLinkAuthenticator#doRegister(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response, java.security.Principal, java.lang.String)
     */
    @Override
    protected void doRegister(Request request, Response response, Principal principal, String password) {
        register(request, response, principal, this.authMethod, principal.getName(), password);        
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.authenticator.AuthenticatorBase#getAuthMethod()
     */
    @Override
    protected String getAuthMethod() {
        return this.authMethod;
    }
}