/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.identity.federation.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Unit Test the XML Time Util
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 6, 2009
 */
public class XMLTimeUtilUnitTestCase {

    @Test
    public void testCompareViaParsing() throws Exception {
        DatatypeFactory dt = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = dt.newXMLGregorianCalendar("2009-06-03T17:42:09.322-04:00");
        XMLGregorianCalendar notBefore = dt.newXMLGregorianCalendar("2009-06-03T17:42:05.901-04:00");
        XMLGregorianCalendar notOnOrAfter = dt.newXMLGregorianCalendar("2009-06-03T17:47:05.901-04:00");
        Assert.assertTrue(XMLTimeUtil.isValid(now, notBefore, notOnOrAfter));
    }

    @Test
    public void testAdd() throws Exception {
        XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();
        long min5 = XMLTimeUtil.inMilis(5);

        XMLGregorianCalendar after5M = XMLTimeUtil.add(now, min5);
        Assert.assertTrue(now.compare(after5M) == DatatypeConstants.LESSER);

        GregorianCalendar nowG = now.toGregorianCalendar();
        GregorianCalendar now5M = after5M.toGregorianCalendar();

        // Add 5 minutes
        nowG.roll(Calendar.MINUTE, 5);

        int val = nowG.compareTo(now5M);

        Assert.assertTrue("Compared value is 0", val <= 0);
    }

    @Test
    public void testNumericCacheDurationValue() throws Exception {
        Duration numericOneSecondeDuration = XMLTimeUtil.parseAsDuration("1000");
        Assert.assertEquals(1, numericOneSecondeDuration.getSeconds());

        Duration numericOneMinuteDuration = XMLTimeUtil.parseAsDuration("60000");
        Assert.assertEquals(1, numericOneMinuteDuration.getMinutes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCacheDurationValue() throws Exception {
        XMLTimeUtil.parseAsDuration("X10A");
    }

    @Test
    public void testISO8601CacheDurationValue() throws Exception {
        Duration numericOneSecondeDuration = XMLTimeUtil.parseAsDuration("P1Y10M3DT1H10M5S");
        Assert.assertEquals(1, numericOneSecondeDuration.getYears());
        Assert.assertEquals(10, numericOneSecondeDuration.getMonths());
        Assert.assertEquals(3, numericOneSecondeDuration.getDays());
        Assert.assertEquals(1, numericOneSecondeDuration.getHours());
        Assert.assertEquals(10, numericOneSecondeDuration.getMinutes());
        Assert.assertEquals(5, numericOneSecondeDuration.getSeconds());
    }

    @Test
    public void testIsValid() throws Exception {
        XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();

        long milisFor5Mins = XMLTimeUtil.inMilis(5);

        XMLGregorianCalendar after5M = XMLTimeUtil.add(now, milisFor5Mins);
        XMLGregorianCalendar after10M = XMLTimeUtil.add(now, milisFor5Mins * 2);

        // isValid(now, notbefore, notOnOrAfter)
        Assert.assertTrue(XMLTimeUtil.isValid(after5M, now, after10M));
        Assert.assertFalse(XMLTimeUtil.isValid(now, after5M, after10M));
    }

    @Test
    public void testGMTFormat() throws Exception {
        String now = XMLTimeUtil.getIssueInstant().toString();
        Assert.assertTrue(now.endsWith("Z"));
        Assert.assertFalse(now.contains("+"));

        System.setProperty(GeneralConstants.TIMEZONE, "GMT+5");
        String now2 = XMLTimeUtil.getIssueInstant().toString();
        Assert.assertTrue(now2.endsWith("+05:00"));

        System.setProperty(GeneralConstants.TIMEZONE, GeneralConstants.TIMEZONE_DEFAULT);
        Assert.assertEquals(XMLTimeUtil.getCurrentTimeZoneID(), TimeZone.getDefault().getID());
    }
}