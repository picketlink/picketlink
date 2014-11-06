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

import java.util.Iterator;
import java.util.Set;

/**
 * Header utilities.
 *
 * @author Giriraj Sharma
 */
public class HeaderUtils {

    /**
     * Serialises the items of a set into a string. Each item must have a meaningful {@code toString()} method.
     *
     * @param set The set to serialise. Must not be {@code null}.
     * @param sep The string separator to apply. Should not be {@code null}.
     *
     * @return The serialised set as string.
     */
    public static String serialize(final Set<String> set, final String sep) {

        StringBuilder sb = new StringBuilder();

        Iterator<String> it = set.iterator();

        while (it.hasNext()) {

            sb.append(it.next());

            if (it.hasNext())
                sb.append(sep);
        }

        return sb.toString();
    }

    /**
     * Parses a header value consisting of zero or more space / comma / space + comma separated strings. The input string is
     * trimmed before splitting.
     *
     * @param headerValue The header value, may be {@code null}.
     *
     * @return A string array of the parsed string items, empty if none were found or the input was {@code null}.
     */
    public static String[] parseMultipleHeaderValues(final String headerValue) {

        if (headerValue == null)
            return new String[0]; // empty array

        String trimmedHeaderValue = headerValue.trim();

        if (trimmedHeaderValue.isEmpty())
            return new String[0];

        return trimmedHeaderValue.split("\\s*,\\s*|\\s+");
    }
}
