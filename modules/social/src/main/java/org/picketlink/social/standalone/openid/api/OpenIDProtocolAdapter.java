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
package org.picketlink.social.standalone.openid.api;

import org.picketlink.social.standalone.openid.api.exceptions.OpenIDProtocolException;

import java.util.Map;


/**
 * Callback adapter sent to the OpenIDManager that implements the protocol behavior such as HTTP
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 6, 2009
 */
public interface OpenIDProtocolAdapter {
    /**
     * Map of attributes to be retrieved from the provider
     *
     * @return
     */
    OpenIDAttributeMap getAttributeMap();

    /**
     * Provide the return url for the OpenIDManager where the Relying Party can handle responses from the OpenID Provider
     *
     * @return
     */
    String getReturnURL();

    /**
     * Send the request to the OpenID Provider
     *
     * @param version OpenID version 1 is via HTTP Redirect and by HTTP Post for version 2
     * @param destinationURL Final Destination URL
     * @param paramMap Map of parameters
     */
    void sendToProvider(int version, String destinationURL, Map<String, String> paramMap) throws OpenIDProtocolException;
}