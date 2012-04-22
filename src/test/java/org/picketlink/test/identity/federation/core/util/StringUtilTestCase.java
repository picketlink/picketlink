/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * Unit test {@link StringUtil}
 * @author Anil Saldhana
 * @since Feb 22, 2012
 */
public class StringUtilTestCase
{
   @Test
   public void testTokenizeKeyValuePairs() throws Exception
   {
      String str = "a=b,c=d,e=f";
      Map<String, String> map = StringUtil.tokenizeKeyValuePair(str);

      assertEquals("b", map.get("a"));
      assertEquals("d", map.get("c"));
      assertEquals("f", map.get("e"));
   }

   @Test
   public void testTokenize() throws Exception
   {
      String str = "a,b";

      String theOtherString = "a/b;c/d";

      List<String> list = StringUtil.tokenize(str);
      assertEquals(2, list.size());
      assertTrue(list.contains("a"));
      assertTrue(list.contains("b"));

      List<String> bigList = StringUtil.tokenize(theOtherString, ";");
      assertEquals(2, bigList.size());
      for (String token : bigList)
      {
         List<String> theList = StringUtil.tokenize(token, "/");

         if (token.equals("a/b"))
         {
            assertTrue(theList.contains("a"));
            assertTrue(theList.contains("b"));
         }
         else if (token.equals("c/d"))
         {
            assertTrue(theList.contains("c"));
            assertTrue(theList.contains("d"));
         }
         else
            throw new RuntimeException("Unknown");
      }
   }

   @Test
   public void trim() throws Exception
   {
      assertNotNull("".trim());
      assertEquals(0, "".trim().length());
      assertEquals(0, StaxParserUtil.trim("").length());
   }
}