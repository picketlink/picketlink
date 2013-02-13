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
package org.picketlink.identity.federation.api.w3.xmldsig;

import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builder for the W3C xml-dsig KeyInfoType
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class KeyInfoBuilder {

    /**
     * Create a KeyInfoType
     *
     * @return
     */
    public static Element createKeyInfo(String id) {
        Document doc = null;
        try {
            doc = DocumentUtil.createDocument();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        Element keyInfoEl = doc.createElementNS(JBossSAMLURIConstants.XMLDSIG_NSURI.get(), JBossSAMLConstants.KEY_INFO.get());
        keyInfoEl.setAttribute("Id", id);
        return keyInfoEl;
    }
}