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
package org.picketlink.test.identity.federation.core.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;

/**
 * Unit Test the XML Time Util
 * @author Anil.Saldhana@redhat.com
 * @since Jan 6, 2009
 */
public class XMLTimeUtilUnitTestCase extends TestCase
{
   public void testCompareViaParsing() throws Exception
   {
      DatatypeFactory dt = DatatypeFactory.newInstance();
      XMLGregorianCalendar now = dt.newXMLGregorianCalendar("2009-06-03T17:42:09.322-04:00");
      XMLGregorianCalendar notBefore = dt.newXMLGregorianCalendar("2009-06-03T17:42:05.901-04:00");
      XMLGregorianCalendar notOnOrAfter = dt.newXMLGregorianCalendar("2009-06-03T17:47:05.901-04:00");
      assertTrue(XMLTimeUtil.isValid(now, notBefore, notOnOrAfter));
   }

   public void testAdd() throws Exception
   {
      XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
      long min5 = XMLTimeUtil.inMilis(5);

      XMLGregorianCalendar after5M = XMLTimeUtil.add(now, min5);
      assertTrue(now.compare(after5M) == DatatypeConstants.LESSER);

      GregorianCalendar nowG = now.toGregorianCalendar();
      GregorianCalendar now5M = after5M.toGregorianCalendar();

      //Add 5 minutes
      nowG.roll(Calendar.MINUTE, 5);

      int val = nowG.compareTo(now5M);

      assertTrue("Compared value is 0", val <= 0);
   }

   public void testIsValid() throws Exception
   {
      XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();

      long milisFor5Mins = XMLTimeUtil.inMilis(5);

      XMLGregorianCalendar after5M = XMLTimeUtil.add(now, milisFor5Mins);
      XMLGregorianCalendar after10M = XMLTimeUtil.add(now, milisFor5Mins * 2);

      //isValid(now, notbefore, notOnOrAfter)
      assertTrue(XMLTimeUtil.isValid(after5M, now, after10M));
      assertFalse(XMLTimeUtil.isValid(now, after5M, after10M));
   }
}