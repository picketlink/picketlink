/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.identity.federation.core.saml.v2.interfaces;

import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

import java.security.Principal;
import java.util.Set;

/**
 * <p>The SAML2AttributeManager is responsible to provide custom attributes, based on the SAML 2.0 version, in order to include them
 * in the assertions issued by an identity provider.</p>
 *
 * @author Pedro Igor
 */
public interface SAML2AttributeManager extends AttributeManager {

    /**
     * <p>Returns a list of {@link org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType} based on a incoming
     * {@link org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType} and the authenticated {@link java.security.Principal}.</p>
     *
     * <p>This gives a lot of flexibility about how attributes are created and populated into the assertion.</p>
     *
     * @param authnRequestType The current authentication request.
     * @param userPrincipal The authenticated principal
     *
     * @return
     */
    Set<AttributeStatementType> getAttributes(AuthnRequestType authnRequestType, Principal userPrincipal);

}
