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
package org.picketlink.social.standalone.oauth;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a mapping of OpenID Attribute alias to type
 *
 * @author Anil Saldhana
 * @since Sep 17, 2011
 */
public class OpenIDAliasMapper {
    private static Map<String, String> map = new HashMap<String, String>();

    static {
        map.put("name", "http://schema.openid.net/namePerson");
        map.put("email", "http://schema.openid.net/contact/email");
        map.put("birthDate", "http://schema.openid.net/birthDate");
        map.put("gender", "http://schema.openid.net/person/gender");
        map.put("postalCode", "http://schema.openid.net/contact/postalCode/home");
        map.put("country", "http://schema.openid.net/contact/country/home");
        map.put("language", "http://schema.openid.net/pref/language");
        map.put("timezone", "http://schema.openid.net/pref/timezone");

        map.put("timezone", "http://schema.openid.net/pref/timezone");

        map.put("ax_email", "http://axschema.org/contact/email");
        map.put("ax_firstName", "http://axschema.org/namePerson/first");
        map.put("ax_lastName", "http://axschema.org/namePerson/last");
        map.put("ax_fullName", "http://axschema.org/namePerson");
    }

    public static String get(String key) {
        return map.get(key);
    }
}