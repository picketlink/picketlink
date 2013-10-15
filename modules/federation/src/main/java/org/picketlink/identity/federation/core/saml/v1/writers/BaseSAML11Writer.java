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
package org.picketlink.identity.federation.core.saml.v1.writers;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

import javax.xml.stream.XMLStreamWriter;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 27, 2011
 */
public abstract class BaseSAML11Writer {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected static String PROTOCOL_PREFIX = "samlp";

    protected static String ASSERTION_PREFIX = "saml";

    protected static String XACML_SAML_PREFIX = "xacml-saml";

    protected static String XACML_SAML_PROTO_PREFIX = "xacml-samlp";

    protected static String XSI_PREFIX = "xsi";

    protected XMLStreamWriter writer;

    public BaseSAML11Writer(XMLStreamWriter writer) {
        this.writer = writer;
    }
}