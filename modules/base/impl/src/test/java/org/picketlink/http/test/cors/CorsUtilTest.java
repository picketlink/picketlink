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
package org.picketlink.http.test.cors;

import junit.framework.TestCase;

import org.picketlink.http.internal.cors.CorsUtil;

public class CorsUtilTest extends TestCase {

    public void testParseMultipleHeaderValues() {

        String[] out = CorsUtil.parseMultipleHeaderValues(null);

        assertEquals(0, out.length);

        out = CorsUtil.parseMultipleHeaderValues("GET, PUT, HEAD");

        assertEquals("GET", out[0]);
        assertEquals("PUT", out[1]);
        assertEquals("HEAD", out[2]);
        assertEquals(3, out.length);

        out = CorsUtil.parseMultipleHeaderValues("GET,PUT,HEAD");

        assertEquals("GET", out[0]);
        assertEquals("PUT", out[1]);
        assertEquals("HEAD", out[2]);
        assertEquals(3, out.length);

        out = CorsUtil.parseMultipleHeaderValues("GET , PUT , HEAD");

        assertEquals("GET", out[0]);
        assertEquals("PUT", out[1]);
        assertEquals("HEAD", out[2]);
        assertEquals(3, out.length);

        out = CorsUtil.parseMultipleHeaderValues("GET PUT HEAD");

        assertEquals("GET", out[0]);
        assertEquals("PUT", out[1]);
        assertEquals("HEAD", out[2]);
        assertEquals(3, out.length);
    }

    public void testFormatCanonical() {

        assertEquals(CorsUtil.formatCanonical("content-type"), "Content-Type");
        assertEquals(CorsUtil.formatCanonical("CONTENT-TYPE"), "Content-Type");
        assertEquals(CorsUtil.formatCanonical("X-type"), "X-Type");
        assertEquals(CorsUtil.formatCanonical("Origin"), "Origin");
        assertEquals(CorsUtil.formatCanonical("A"), "A");

        try {
            assertEquals(CorsUtil.formatCanonical(""), "");
            fail("Failed to raise IllegalArgumentException on empty string");

        } catch (IllegalArgumentException e) {
            // ok
        }
    }
}