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
package org.picketlink.test.scim.parsing;

import org.junit.Test;
import org.picketlink.scim.codec.SCIMParser;
import org.picketlink.scim.model.v11.Meta;
import org.picketlink.scim.model.v11.SCIMUser;
import org.picketlink.scim.model.v11.SCIMUser.Emails;
import org.picketlink.scim.model.v11.SCIMUser.Ims;
import org.picketlink.scim.model.v11.UserName;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Validate parsing of SCIM user representation
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class UserParsingTestCase {

    @Test
    public void parse() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("json/user.json");
        assertNotNull(is);

        SCIMParser parser = new SCIMParser();
        SCIMUser user = parser.parseUser(is);
        assertNotNull(user);

        assertEquals("2819c223-7f76-453a-919d-413861904646", user.getId());
        assertEquals("701984", user.getExternalId());
        assertEquals("Ms. Barbara J Jensen III", user.getName().getFormatted());

        // Validate User Name
        UserName userName = user.getName();
        assertEquals("Ms. Barbara J Jensen III", userName.getFormatted());
        assertEquals("Jensen", userName.getFamilyName());
        assertEquals("Barbara", userName.getGivenName());
        assertEquals("Jane", userName.getMiddleName());
        assertEquals("Ms.", userName.getHonorificPrefix());
        assertEquals("III", userName.getHonorificSuffix());

        assertEquals("Babs Jensen", user.getDisplayName());
        assertEquals("Babs", user.getNickName());
        assertEquals("https://login.example.com/bjensen", user.getProfileUrl());

        Emails[] emails = user.getEmails();
        assertEquals(2, emails.length);

        assertEquals(2, user.getAddresses().length);

        assertEquals(2, user.getPhoneNumbers().length);
        Ims[] ims = user.getIms();

        assertEquals(1, ims.length);
        Ims im = ims[0];
        assertEquals("someaimhandle", im.getValue());
        assertEquals("aim", im.getType());

        assertEquals(2, user.getPhotos().length);

        assertEquals("Employee", user.getUserType());
        assertEquals("Tour Guide", user.getTitle());
        assertEquals("en_US", user.getPreferredLanguage());
        assertEquals("en_US", user.getLocale());
        assertEquals("America/Los_Angeles", user.getTimezone());
        assertTrue(user.isActive());

        assertEquals("t1meMa$heen", user.getPassword());

        assertEquals(3, user.getGroups().length);

        assertEquals(1, user.getX509Certificates().length);

        Meta meta = user.getMeta();
        assertEquals("2010-01-23T04:56:22Z", meta.getCreated());
        assertEquals("2011-05-13T04:42:34Z", meta.getLastModified());
        assertEquals("W\"a330bc54f0671c9\"", meta.getVersion());
        assertEquals("https://example.com/v1/Users/2819c223-7f76-453a-919d-413861904646", meta.getLocation());
    }
}