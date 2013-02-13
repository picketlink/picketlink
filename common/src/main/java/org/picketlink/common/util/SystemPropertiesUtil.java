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
package org.picketlink.common.util;

import javax.xml.XMLConstants;

/**
 * Utility dealing with the system properties at the JVM level for PicketLink
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SystemPropertiesUtil {
    static {
        // XML Signature
        String xmlSec = "org.apache.xml.security.ignoreLineBreaks";
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xmlSec, ""))) {
            SecurityActions.setSystemProperty(xmlSec, "true");
        }

        // For JAXP Validation
        String schemaFactoryProperty = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(schemaFactoryProperty, ""))) {
            SecurityActions.setSystemProperty(schemaFactoryProperty, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        }

        // For the XACML Engine
        String xacmlValidation = "org.jboss.security.xacml.schema.validation";
        if (StringUtil.isNullOrEmpty(SecurityActions.getSystemProperty(xacmlValidation, ""))) {
            SecurityActions.setSystemProperty(xacmlValidation, "false");
        }
    };

    /**
     * No-op call such that the default system properties are set
     */
    public static void ensure() {
    }
}