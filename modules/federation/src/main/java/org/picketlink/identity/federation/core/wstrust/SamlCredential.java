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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.common.util.TransformerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Credential that wraps a SAML Assertion.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public final class SamlCredential implements Serializable {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private static final long serialVersionUID = -8496414959425288835L;

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerUtil.getTransformerFactory();

    private final String assertion;

    public SamlCredential(final Element assertion) {
        if (assertion == null)
            throw logger.nullArgumentError("assertion");

        this.assertion = SamlCredential.assertionToString(assertion);
    }

    public SamlCredential(final String assertion) {
        if (StringUtil.isNullOrEmpty(assertion))
            throw logger.nullArgumentError("assertion");

        this.assertion = assertion;
    }

    public String getAssertionAsString() {
        return assertion;
    }

    public Element getAssertionAsElement() throws ProcessingException {
        return SamlCredential.assertionToElement(assertion);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof SamlCredential))
            return false;

        final SamlCredential that = (SamlCredential) obj;
        return this.assertion.equals(that.assertion);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + assertion.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SamlCredential[" + assertion + "]";
    }

    public static Element assertionToElement(final String assertion) throws ProcessingException {
        try {
            Document document = DocumentUtil.getDocument(assertion);
            return (Element) document.getFirstChild();
        } catch (final ConfigurationException e) {
            throw logger.processingError(e);
        } catch (final ParsingException e) {
            throw logger.processingError(e);
        }
    }

    public static String assertionToString(final Element assertion) {
        if (assertion == null)
            throw logger.nullArgumentError("assertion");

        try {
            final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            final Source source = new DOMSource(assertion);
            final StringWriter writer = new StringWriter();
            final Result result = new StreamResult(writer);

            transformer.transform(source, result);

            return writer.toString();
        } catch (final TransformerException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}