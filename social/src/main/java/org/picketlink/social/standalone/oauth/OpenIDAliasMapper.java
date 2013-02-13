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