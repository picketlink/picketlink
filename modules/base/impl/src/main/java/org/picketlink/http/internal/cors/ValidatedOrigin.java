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
package org.picketlink.http.internal.cors;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.IDN;

/**
 * Validated resource request origin, as defined in The Web Origin Concept (RFC 6454). Supported schemes are {@code http} and
 * {@code https}.
 *
 * @author Giriraj Sharma
 */
public class ValidatedOrigin extends Origin {

    /**
     * The origin scheme.
     */
    private String scheme;

    /**
     * The origin host.
     */
    private String host;

    /**
     * The parsed origin port, -1 for default port.
     */
    private int port = -1;

    /**
     * Creates a new validated origin from the specified URI string.
     *
     * @param value The URI string for the origin. Must not be {@code null}.
     *
     * @throws OriginException If the value doesn't represent a valid and supported origin string.
     */
    public ValidatedOrigin(final String value) throws OriginException {

        super(value);

        // Parse URI value
        URI uri = null;

        try {
            uri = new URI(value);

        } catch (URISyntaxException e) {
            throw new OriginException("Bad origin URI: " + e.getMessage());
        }

        scheme = uri.getScheme();
        host = uri.getHost();
        port = uri.getPort();

        if (scheme == null)
            throw new OriginException("Bad origin URI: Missing scheme, such as http or https");

        // Canonicalise scheme and host
        scheme = scheme.toLowerCase();

        // Apply the IDNA toASCII algorithm [RFC3490] to /host/
        host = IDN.toASCII(host, IDN.ALLOW_UNASSIGNED | IDN.USE_STD3_ASCII_RULES);

        // Finally, convert to lower case
        host = host.toLowerCase();
    }

    /**
     * Returns the scheme.
     *
     * @return The scheme.
     */
    public String getScheme() {

        return scheme;
    }

    /**
     * Returns the host (name or IP address).
     *
     * @return The host name or IP address.
     */
    public String getHost() {

        return host;
    }

    /**
     * Returns the port number.
     *
     * @return The port number, -1 for default port.
     */
    public int getPort() {

        return port;
    }

    /**
     * Returns the suffix which is made up of the host name / IP address and port (if a non-default port is specified).
     *
     * <p>
     * Example:
     *
     * <pre>
     * http://example.com => example.com
     * http://example.com:8080 => example.com:8080
     * </pre>
     *
     * @return The suffix.
     */
    public String getSuffix() {

        String s = host;

        if (port != -1)
            s = s + ":" + port;

        return s;
    }
}
