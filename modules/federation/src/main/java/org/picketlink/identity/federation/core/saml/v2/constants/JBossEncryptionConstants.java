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
package org.picketlink.identity.federation.core.saml.v2.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Encryption Algorithm and XMLEnC URI
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 4, 2009
 */
public class JBossEncryptionConstants {

    private static Map<String, String> algoToXmlEncURL = new HashMap<String, String>();

    static {
        algoToXmlEncURL.put("DESede", "http://www.w3.org/2001/04/xmlenc#kw-tripledes");
        algoToXmlEncURL.put("TRIPLEDES", "http://www.w3.org/2001/04/xmlenc#kw-tripledes");

        algoToXmlEncURL.put("AES_128", "http://www.w3.org/2001/04/xmlenc#aes128-cbc");
        algoToXmlEncURL.put("AES_192", "http://www.w3.org/2001/04/xmlenc#aes192-cbc");
        algoToXmlEncURL.put("AES_256", "http://www.w3.org/2001/04/xmlenc#aes256-cbc");
    }

    public static String getURL(String algo, int keySize) {
        if (keySize == 0)
            return algoToXmlEncURL.get(algo);
        return algoToXmlEncURL.get(algo + "_" + keySize);
    }
}
