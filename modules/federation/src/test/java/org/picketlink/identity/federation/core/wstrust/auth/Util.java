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
package org.picketlink.identity.federation.core.wstrust.auth;

import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLBaseFactory;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Test util methods.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public final class Util {

    private Util() {
    }

    public static Element createSamlToken() throws Exception {
        String id = "ID+" + JBossSAMLBaseFactory.createUUID();
        final AssertionType assertionType = new AssertionType(id, XMLTimeUtil.getIssueInstant());
        return SAMLUtil.toElement(assertionType);
    }

    public static Map<String, String> allOptions() {
        Map<String, String> options = new HashMap<String, String>();
        options.put(AbstractSTSLoginModule.STS_CONFIG_FILE, "wstrust/auth/jboss-sts-client.properties");
        return options;
    }

}
