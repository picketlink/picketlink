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
package org.picketlink.identity.federation.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.SystemPropertiesUtil;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utility class associated with JAXP Validation
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 30, 2011
 */
public class JAXPValidationUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    protected static Validator validator;

    protected static SchemaFactory schemaFactory;

    public static void validate(String str) throws SAXException, IOException {
        validator().validate(new StreamSource(str));
    }

    public static void validate(InputStream stream) throws SAXException, IOException {
        validator().validate(new StreamSource(stream));
    }

    /**
     * Based on system property "picketlink.schema.validate" set to "true", do schema validation
     *
     * @param samlDocument
     * @throws ProcessingException
     */
    public static void checkSchemaValidation(Node samlDocument) throws ProcessingException {
        if (SecurityActions.getSystemProperty("picketlink.schema.validate", "false").equalsIgnoreCase("true")) {
            try {
                JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(samlDocument));
            } catch (Exception e) {
                throw logger.processingError(e);
            }
        }
    }

    public static Validator validator() throws SAXException, IOException {
        SystemPropertiesUtil.ensure();

        if (validator == null) {
            Schema schema = getSchema();
            if (schema == null)
                throw logger.nullValueError("schema");

            validator = schema.newValidator();
            validator.setErrorHandler(new CustomErrorHandler());
        }
        return validator;
    }

    private static Schema getSchema() throws IOException {
        schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        schemaFactory.setResourceResolver(new IDFedLSInputResolver());
        schemaFactory.setErrorHandler(new CustomErrorHandler());
        Schema schemaGrammar = null;
        try {
            schemaGrammar = schemaFactory.newSchema(sources());
        } catch (SAXException e) {
            logger.xmlCouldNotGetSchema(e);
        }
        return schemaGrammar;
    }

    private static Source[] sources() throws IOException {
        List<String> schemas = SchemaManagerUtil.getSchemas();

        Source[] sourceArr = new Source[schemas.size()];

        int i = 0;
        for (String schema : schemas) {
            URL url = SecurityActions.loadResource(JAXPValidationUtil.class, schema);
            if (url == null)
                throw logger.nullValueError("schema url:" + schema);
            sourceArr[i++] = new StreamSource(url.openStream());
        }
        return sourceArr;
    }

    private static class CustomErrorHandler implements ErrorHandler {
        public void error(SAXParseException ex) throws SAXException {
            logException(ex);
            if (ex.getMessage().contains("null") == false) {
                throw ex;
            }
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            logException(ex);
            throw ex;
        }

        public void warning(SAXParseException ex) throws SAXException {
            logException(ex);
        }

        private void logException(SAXParseException sax) {
            StringBuilder builder = new StringBuilder();

            if (logger.isTraceEnabled()) {
                builder.append("[line:").append(sax.getLineNumber()).append(",").append("::col=").append(sax.getColumnNumber())
                        .append("]");
                builder.append("[publicID:").append(sax.getPublicId()).append(",systemId=").append(sax.getSystemId())
                        .append("]");
                builder.append(":").append(sax.getLocalizedMessage());
                logger.trace(builder.toString());
            }
        }
    };
}