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

package org.picketlink.identity.federation.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class in process of parsing signature out of SAML token.
 * usage example:
 *   <code> 
 *   xpath.setNamespaceContext(
 *       NamespaceContext.create()
 *           .addNsUriPair(xmlSignatureNSPrefix, JBossSAMLURIConstants.XMLDSIG_NSURI.get())
 *   );
 *   </code>
 *                       
 *
 * 
 * @author Peter Skopek: pskopek at redhat dot com
 * 
 */

public class NamespaceContext implements javax.xml.namespace.NamespaceContext {

    private Map<String, String> nsMap = new HashMap<String, String>();

    public NamespaceContext() {
    }

    public NamespaceContext(String prefix, String uri) {
        nsMap.put(prefix, uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        return nsMap.get(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceURI) {
        for (String key : nsMap.keySet()) {
            String value = nsMap.get(key);
            if (value.equals(namespaceURI)) {
                return key;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
        return nsMap.keySet().iterator();
    }

    public NamespaceContext addNsUriPair(String ns, String uri) {
        nsMap.put(ns, uri);
        return this;
    }

    /**
     * Create new NamespaceContext for use. 
     * @return
     */
    public static NamespaceContext create() {
        return new NamespaceContext();
    }
}
