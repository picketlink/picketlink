/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.acl.Group;
import java.util.List;

import javax.security.auth.Subject;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkGroup;

/**
 * Privileged actions.
 *
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @author Anil Saldhana
 * @version $Revision: 1 $
 */
class SecurityActions {
    static SecurityContext createSecurityContext(final Principal p, final Object cred, final Subject subject) {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>() {
                public SecurityContext run() {
                    SecurityContext sc = null;
                    try {
                        sc = SecurityContextFactory.createSecurityContext(p, cred, subject, "SAML2_HANDLER");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return sc;
                }
            });
        } else {
            SecurityContext sc = null;
            try {
                sc = SecurityContextFactory.createSecurityContext(p, cred, subject, "SAML2_HANDLER");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return sc;
        }
    }

    static void setSecurityContext(final SecurityContext sc) {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    SecurityContextAssociation.setSecurityContext(sc);
                    return null;
                }
            });
        } else {
            SecurityContextAssociation.setSecurityContext(sc);
        }
    }

    static SecurityContext getSecurityContext() {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>() {
                public SecurityContext run() {
                    return SecurityContextAssociation.getSecurityContext();
                }
            });
        } else {
            return SecurityContextAssociation.getSecurityContext();
        }
    }

    /**
     * Get the {@link Subject} from the {@link SecurityContextAssociation}
     *
     * @return authenticated subject or null
     */
    static Subject getAuthenticatedSubject() {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<Subject>() {
                public Subject run() {
                    SecurityContext sc = SecurityContextAssociation.getSecurityContext();
                    if (sc != null)
                        return sc.getUtil().getSubject();
                    return null;
                }
            });
        } else {
            SecurityContext sc = SecurityContextAssociation.getSecurityContext();
            if (sc != null)
                return sc.getUtil().getSubject();
            return null;
        }
    }

    /**
     * <p>Returns a system property value using the specified <code>key</code>. If not found the <code>defaultValue</code> will be returned.</p>
     *
     * @param key
     * @param defaultValue
     * @return
     */
    static String getSystemProperty(final String key, final String defaultValue) {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(key, defaultValue);
                }
            });
        } else {
            return System.getProperty(key, defaultValue);
        }
    }

    static ClassLoader getClassLoader(final Class<?> clazz) {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return clazz.getClassLoader();
                }
            });
        } else {
            return clazz.getClassLoader();
        }
    }

    static ClassLoader getContextClassLoader() {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {        
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    /**
     * Given a {@link List} of role names, construct a group principal of type {@link Group}
     *
     * @param roles
     * @return
     */
    static Group group(final List<String> roles) {
        SecurityManager sm = System.getSecurityManager();
        
        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<Group>() {
                public Group run() {
                    Group theGroup = new PicketLinkGroup(SecurityConstants.ROLES_IDENTIFIER);
                    for (String role : roles) {
                        theGroup.addMember(new SimplePrincipal(role));
                    }
                    return theGroup;
                }
            });
        } else {
            Group theGroup = new PicketLinkGroup(SecurityConstants.ROLES_IDENTIFIER);
            for (String role : roles) {
                theGroup.addMember(new SimplePrincipal(role));
            }
            return theGroup;
        }
    }
}