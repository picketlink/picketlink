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


import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.picketlink.http.internal.cors.HeaderUtils;

/**
 * Tests the header utilities.
 *
 * @author Giriraj Sharma
 */
public class HeaderUtilsTest extends TestCase {

	public void testSerialize() {

		Set<String> values = new LinkedHashSet<String>();
		values.add("apples");
		values.add("pears");
		values.add("oranges");

		String out = HeaderUtils.serialize(values, ", ");

		assertEquals("apples, pears, oranges", out);

		out = HeaderUtils.serialize(values, " ");

		assertEquals("apples pears oranges", out);

		out = HeaderUtils.serialize(values, null);

		assertEquals("applesnullpearsnulloranges", out);
	}


	public void testParseMultipleHeaderValues() {

		String[] out = HeaderUtils.parseMultipleHeaderValues(null);

		assertEquals(0, out.length);

		out = HeaderUtils.parseMultipleHeaderValues("apples, pears, oranges");

		assertEquals("apples", out[0]);
		assertEquals("pears", out[1]);
		assertEquals("oranges", out[2]);
		assertEquals(3, out.length);

		out = HeaderUtils.parseMultipleHeaderValues("apples,pears,oranges");

		assertEquals("apples", out[0]);
		assertEquals("pears", out[1]);
		assertEquals("oranges", out[2]);
		assertEquals(3, out.length);

		out = HeaderUtils.parseMultipleHeaderValues("apples pears oranges");

		assertEquals("apples", out[0]);
		assertEquals("pears", out[1]);
		assertEquals("oranges", out[2]);
		assertEquals(3, out.length);
	}

}
