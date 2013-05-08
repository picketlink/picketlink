/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.bindings.jboss.subject;

import java.io.Serializable;
import java.security.Principal;

import org.jboss.security.SimplePrincipal;

/**
 * Simple Principal
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 16, 2009
 */
public class PicketLinkPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 1L;

    protected String name;

    private static final String OVERRIDE_EQUALS_BEHAVIOR = "org.picketlink.principal.equals.override";

    public PicketLinkPrincipal(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return (this.name == null ? 0 : this.name.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Principal))
            return false;

        // if the org.picketlink.principal.equals.override system property has been set, narrow the allowed type.
        if ("true".equals(SecurityActions.getSystemProperty(OVERRIDE_EQUALS_BEHAVIOR, "false"))) {
            if (!(obj instanceof SimplePrincipal))
                return false;
        }

        // compare the principal names.
        String anotherName = ((Principal) obj).getName();
        boolean equals = false;
        if (this.name == null)
            equals = anotherName == null;
        else
            equals = this.name.equals(anotherName);
        return equals;
    }

    @Override
    public String toString() {
        return this.name;
    }
}