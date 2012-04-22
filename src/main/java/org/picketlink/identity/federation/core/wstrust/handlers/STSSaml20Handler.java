/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust.handlers;

import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.WSSE_NS;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.SAML2_ASSERTION_NS;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;


/**
 * A concrete implementation of {@link STSSecurityHandler} that can handle SAML 
 * version 2.0 Assertion inside of {@link WSTrustConstants#WSSE_NS} elements.
 * <p/>
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSSaml20Handler extends STSSecurityHandler
{
    /**
     * Qualified name for WSSE Security Header ({@link WSTrustConstants#WSSE_NS}:"Security")
     */
    public static final QName SECURITY_QNAME = new QName(WSSE_NS, "Security");
    
    /**
     * Qualified name for SAML Version 2.0 ({@link WSTrustConstants#SAML2_ASSERTION_NS}:"Assertion")
     */
    public static final QName SAML_TOKEN_QNAME = new QName(SAML2_ASSERTION_NS, "Assertion");
    
    /*
     * (non-Javadoc)
     * @see org.picketlink.identity.federation.api.wstrust.handlers.PicketLinkSTSSecurityHandler#getSecurityElementQName()
     */
    @Override
    public QName getSecurityElementQName()
    {
        return SECURITY_QNAME;
    }

    /*
     * (non-Javadoc)
     * @see org.picketlink.identity.federation.api.wstrust.handlers.PicketLinkSTSSecurityHandler#getTokenElementQName()
     */
    @Override
    public QName getTokenElementQName()
    {
        return SAML_TOKEN_QNAME;
    }

}
