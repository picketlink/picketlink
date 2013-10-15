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
package org.picketlink.identity.federation.core.saml.v2.util;

import org.jboss.security.xacml.core.model.context.ObjectFactory;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.TransformerUtil;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

/**
 * Utility for SAML and XACML
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 20, 2010
 */
public class SAMLXACMLUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String XACML_PKG_PATH = "org.jboss.security.xacml.core.model.context";

    public static JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(XACML_PKG_PATH);
    }

    public static Document getXACMLResponse(ResponseType responseType) throws ProcessingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXBElement<?> jaxb = (new ObjectFactory()).createResponse(responseType);

        StreamResult result = new StreamResult(baos);

        try {
            TransformerUtil.transform(SAMLXACMLUtil.getJAXBContext(), jaxb, result);
            return DocumentUtil.getDocument(new String(baos.toByteArray()));
        } catch (Exception e) {
            throw logger.processingError(e);
        }
    }

    public static Document getXACMLRequest(RequestType requestType) throws ProcessingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Marshaller marshaller = getMarshaller();
        JAXBElement<?> jaxb = (new ObjectFactory()).createRequest(requestType);

        StreamResult result = new StreamResult(baos);

        try {
            TransformerUtil.transform(getJAXBContext(), jaxb, result);
            return DocumentUtil.getDocument(new String(baos.toByteArray()));
        } catch (Exception e) {
            throw logger.processingError(e);
        }
    }
}