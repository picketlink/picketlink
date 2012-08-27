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

package org.picketlink.identity.federation.bindings.jboss.auth;

import org.jboss.security.auth.callback.ObjectCallback;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;

/**
 * Callback that can deal with token compression based on initial argument.
 * 
 * @author Peter Skopek: pskopek at redhat dot com
 *
 */
public class SAMLCallback extends ObjectCallback {

    /**
     * Token compression type: gzip
     */
    public static final String GZIP_TOKEN_COMPRESSION = "gzip"; 

    /**
     * Token compression type: none 
     * No compression.
     */
    public static final String NONE_TOKEN_COMPRESSION = "none"; 
           

    private String tokenCompression = NONE_TOKEN_COMPRESSION;

    public SAMLCallback(String tokenCompression) {
        super(null);
        if (tokenCompression != null) {
            this.tokenCompression = tokenCompression;
        }
    }

    @Override
    public void setCredential(Object credential) {
        if (tokenCompression.equals(GZIP_TOKEN_COMPRESSION)) {
            if (credential instanceof SamlCredential) {
                String base64Gzipped = ((SamlCredential) credential).getAssertionAsString();
                byte[] decompressed = Base64.decode(base64Gzipped);
                super.setCredential(new SamlCredential(new String(decompressed)));
            }
            else {
                // TODO: change throwing to PL logger
                throw new RuntimeException("When compression is enabled, received credential should be SamlCredential, but got " 
                        + credential.getClass().getName());   
            }
        }
        else {
            super.setCredential(credential);
        }
    }

    
}
