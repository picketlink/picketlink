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

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.picketlink.http.internal.cors.CorsUtil;

/**
 * Tests the CORS Utility methods.
 *
 * @author Giriraj Sharma
 */
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

        out = CorsUtil.parseMultipleHeaderValues("GET ,    PUT,       HEAD");

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

    public void testTrim() {
        String expected = "Content-Type";
        String n1 = CorsUtil.formatCanonical("content-type\n");
        String n2 = CorsUtil.formatCanonical(" CONTEnt-Type ");

        assertEquals("All whitespace should be trimmed", expected, n1);
        assertEquals("All whitespace should be trimmed", expected, n2);
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

    public void testInvalid1() {
        assertInvalid("X-r@b");
    }

    public void testInvalid2() {
        assertInvalid("1=X-r");
    }

    public void testInvalid3() {
        assertInvalid("Aaa Bbb");
    }

    public void testInvalid4() {
        assertInvalid("less<than");
    }

    public void testInvalid5() {
        assertInvalid("alpha1>");
    }

    public void testInvalid6() {
        assertInvalid("X-Forwarded-By-{");
    }

    public void testInvalid7() {
        assertInvalid("a}");
    }

    public void testInvalid8() {
        assertInvalid("separator:");
    }

    public void testInvalid9() {
        assertInvalid("asd\"f;");
    }

    public void testInvalid10() {
        assertInvalid("rfc@w3c.org");
    }

    public void testInvalid11() {
        assertInvalid("bracket[");
    }

    public void testInvalid12() {
        assertInvalid("control\u0002header");
    }

    public void testInvalid13() {
        assertInvalid("control\nembedded");
    }

    public void testInvalid14() {
        assertInvalid("uni╚(•⌂•)╝");
    }

    public void testInvalid15() {
        assertInvalid("uni\u3232_\u3232");
    }

    public void testUnusualButValid() {
        CorsUtil.formatCanonical("__2");
        CorsUtil.formatCanonical("$%.%");
        CorsUtil.formatCanonical("`~'&#*!^|");
        CorsUtil.formatCanonical("Original_Name");
    }

    private void assertInvalid(String header) {
        try {
            CorsUtil.formatCanonical(header);
            fail("Failed to raise exeption on bad header name");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    public void testJoin() {
        Set<String> supportedMethods = new HashSet<String>();
        supportedMethods.add("HEAD");
        supportedMethods.add("PUT");
        supportedMethods.add("DELETE");

        assertEquals(CorsUtil.join(supportedMethods), "DELETE, PUT, HEAD");
    }

}