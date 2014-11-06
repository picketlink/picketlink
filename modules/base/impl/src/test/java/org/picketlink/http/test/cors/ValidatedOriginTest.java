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

import org.picketlink.http.internal.cors.OriginException;
import org.picketlink.http.internal.cors.ValidatedOrigin;

import junit.framework.TestCase;

/**
 * Tests the validated origin class.
 *
 * @author Giriraj Sharma
 */
public class ValidatedOriginTest extends TestCase {

    public void testHTTPOrigin() {

        String uri = "http://example.com";

        ValidatedOrigin o = null;

        try {
            o = new ValidatedOrigin(uri);
        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertEquals(uri, o.toString());

        assertEquals("http", o.getScheme());
        assertEquals("example.com", o.getHost());
        assertEquals(-1, o.getPort());
        assertEquals("example.com", o.getSuffix());
    }

    public void testHTTPSOrigin() {

        String uri = "https://example.com";

        ValidatedOrigin o = null;

        try {
            o = new ValidatedOrigin(uri);

        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertEquals(uri, o.toString());

        assertEquals("https", o.getScheme());
        assertEquals("example.com", o.getHost());
        assertEquals(-1, o.getPort());
        assertEquals("example.com", o.getSuffix());
    }

    public void testAPPOrigin() {

        String uri = "app://example.com";

        ValidatedOrigin o = null;

        try {
            o = new ValidatedOrigin(uri);
        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertEquals(uri, o.toString());

        assertEquals("app", o.getScheme());
        assertEquals("example.com", o.getHost());
        assertEquals(-1, o.getPort());
        assertEquals("example.com", o.getSuffix());
    }

    public void testIPAddressHost() {

        String uri = "http://192.168.0.1:8080";

        ValidatedOrigin o = null;

        try {
            o = new ValidatedOrigin(uri);
        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertEquals(uri, o.toString());

        assertEquals("http", o.getScheme());
        assertEquals("192.168.0.1", o.getHost());
        assertEquals(8080, o.getPort());
        assertEquals("192.168.0.1:8080", o.getSuffix());
    }
}
