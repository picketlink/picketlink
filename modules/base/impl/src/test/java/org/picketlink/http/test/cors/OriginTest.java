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

import org.picketlink.http.internal.cors.Origin;
import org.picketlink.http.internal.cors.OriginException;
import org.picketlink.http.internal.cors.ValidatedOrigin;

import junit.framework.TestCase;

/**
 * Tests the base origin class.
 *
 * @author Giriraj Sharma
 */
public class OriginTest extends TestCase {

    public void testOrigin() {

        String uri = "http://example.com";

        Origin o = new Origin(uri);

        assertEquals(uri, o.toString());
        assertEquals(uri.hashCode(), o.hashCode());
    }

    public void testOriginEquality() {

        String uri = "http://example.com";

        Origin o1 = new Origin(uri);
        Origin o2 = new Origin(uri);

        assertTrue(o1.equals(o2));
    }

    public void testOriginInequality() {

        String uri1 = "http://example.com";
        String uri2 = "HTTP://EXAMPLE.COM";

        Origin o1 = new Origin(uri1);
        Origin o2 = new Origin(uri2);

        assertFalse(o1.equals(o2));
    }

    public void testOriginInequalityNull() {

        assertFalse(new Origin("http://example.com").equals(null));
    }

    public void testValidation() {

        String uri = "http://example.com";

        ValidatedOrigin validatedOrigin = null;

        try {
            validatedOrigin = new Origin(uri).validate();
        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertNotNull(validatedOrigin);

        assertEquals(uri, validatedOrigin.toString());
    }

    public void testValidationAppScheme() {

        String uri = "app://example.com";

        ValidatedOrigin validatedOrigin = null;

        try {
            validatedOrigin = new Origin(uri).validate();
        } catch (OriginException e) {
            fail(e.getMessage());
        }

        assertNotNull(validatedOrigin);

        assertEquals(uri, validatedOrigin.toString());
    }
}
