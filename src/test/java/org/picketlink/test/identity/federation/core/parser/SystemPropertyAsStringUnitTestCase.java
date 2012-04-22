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
package org.picketlink.test.identity.federation.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * Unit Test {@link StringUtil#getSystemPropertyAsString(String)}
 * that parses a string that represents a system property
 * @author Anil.Saldhana@redhat.com
 * @since Feb 22, 2011
 */
public class SystemPropertyAsStringUnitTestCase
{
   @Before
   public void setup()
   {
      System.setProperty("test", "anil");
      System.setProperty("person", "marcus");
   }

   @Test
   public void testSystemProperty() throws Exception
   {
      try
      {
         assertEquals(null, StringUtil.getSystemPropertyAsString(null));
         fail("should not have passed");
      }
      catch (IllegalArgumentException iae)
      {

      }
      catch (Exception e)
      {
         fail("unknown ex");
      }
      assertEquals("test", StringUtil.getSystemPropertyAsString("test"));
      assertEquals("test/test", StringUtil.getSystemPropertyAsString("test/test"));

      assertEquals("anil", StringUtil.getSystemPropertyAsString("${test::something}"));

      assertEquals("anil", StringUtil.getSystemPropertyAsString("${test}"));
      assertEquals("test/anil", StringUtil.getSystemPropertyAsString("test/${test}"));

      assertEquals("anil:anil:marcus//anil", StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test}"));

      //Test if any of the parantheses are not correctly closed
      assertEquals("anil:anil:marcus//${test",
            StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test"));

      //Test the default values
      assertEquals("http://something", StringUtil.getSystemPropertyAsString("${dummy::http://something}"));
      assertEquals("http://something__hi",
            StringUtil.getSystemPropertyAsString("${dummy::http://something}__${to::hi}"));
      assertEquals("anil:anil:marcus//anilhi",
            StringUtil.getSystemPropertyAsString("${test}:${test}:${person}//${test}${to::hi}"));
      assertEquals("anil:anil:marcus//anilhihttp://something",
            StringUtil
                  .getSystemPropertyAsString("${test}:${test}:${person}//${test}${to::hi}${dummy::http://something}"));
   }
}