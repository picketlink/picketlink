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
package org.picketlink.identity.federation.bindings.tomcat.sp;

import org.apache.catalina.LifecycleException;


/**
 * Authenticator for SAML 1.1 processing at the Service Provider
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 7, 2011
 */
public class SAML11SPRedirectFormAuthenticator extends AbstractSAML11SPRedirectFormAuthenticator {
    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.BaseFormAuthenticator#start()
     */
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        startPicketLink();
    }

    public void testStart() throws LifecycleException {
        super.testStart();
        startPicketLink();
    }

    @Override
    protected String getContextPath() {
        return getContext().getServletContext().getContextPath();
    }
}