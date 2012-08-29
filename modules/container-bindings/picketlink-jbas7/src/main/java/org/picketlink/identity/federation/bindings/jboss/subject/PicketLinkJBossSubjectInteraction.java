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
package org.picketlink.identity.federation.bindings.jboss.subject;

import java.security.Principal;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.security.CacheableManager;
import org.jboss.security.SecurityConstants;
import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction;

/**
 * An implementation of {@link SubjectSecurityInteraction} for JBoss AS 7.
 *
 * @author Anil.Saldhana@redhat.com
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @since Sep 13, 2011
 */
public class PicketLinkJBossSubjectInteraction implements SubjectSecurityInteraction {
    
    private String securityDomain;
    
    /**
     * @see org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction#cleanup(java.security.Principal)
     */
    public boolean cleanup(Principal principal) {
        try {
            String lookupDomain = this.securityDomain;
            
            if (lookupDomain.startsWith(SecurityConstants.JAAS_CONTEXT_ROOT) == false)
                lookupDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + lookupDomain;

            // lookup the JBossCachedAuthManager.
            InitialContext context = new InitialContext();
            CacheableManager manager = (CacheableManager) context.lookup(lookupDomain);

            // Flush the Authentication Cache
            manager.flushCache(principal);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * @see org.picketlink.identity.federation.bindings.tomcat.SubjectSecurityInteraction#get()
     */
    public Subject get() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (PolicyContextException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }
}