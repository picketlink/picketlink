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
package org.picketlink.common.parsers;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.SystemPropertiesUtil;

/**
 * Base class for parsers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public abstract class AbstractParser implements ParserNamespaceSupport {
    
    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
      * Get the JAXP {@link XMLInputFactory}
      * @return
      */
      protected XMLInputFactory getXMLInputFactory() {
         boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false")
                 .equalsIgnoreCase("true");
          ClassLoader prevTCCL = getTCCL();
        try {
             if (tccl_jaxp) {
             setTCCL(getClass().getClassLoader());
              }
               return XMLInputFactory.newInstance();
           } finally {
               if (tccl_jaxp) {
              setTCCL(prevTCCL);
             }
           }
    }

    /**
     * Parse an InputStream for payload
     *
     * @param configStream
     * @return
     * @throws {@link IllegalArgumentException}
     * @throws {@link IllegalArgumentException} when the configStream is null
     */
    public Object parse(InputStream configStream) throws ParsingException {
        if (configStream == null)
            throw logger.nullArgumentError("InputStream");

        XMLInputFactory xmlInputFactory = getXMLInputFactory();

        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(configStream);

        try {
            xmlEventReader = xmlInputFactory.createFilteredReader(xmlEventReader, new EventFilter() {
                public boolean accept(XMLEvent xmlEvent) {
                    // We are going to disregard characters that are new line and whitespace
                    if (xmlEvent.isCharacters()) {
                        Characters chars = xmlEvent.asCharacters();
                        String data = chars.getData();
                        data = valid(data) ? data.trim() : null;
                        return valid(data);
                    } else {
                        return xmlEvent.isStartElement() || xmlEvent.isEndElement();
                    }
                }

                private boolean valid(String str) {
                    return str != null && str.length() > 0;
                }
            });
        } catch (XMLStreamException e) {
            throw logger.parserException(e);
        }

        return parse(xmlEventReader);
    }

    private ClassLoader getTCCL() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    private void setTCCL(final ClassLoader paramCl) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    Thread.currentThread().setContextClassLoader(paramCl);
                    return null;
                }
            });
        } else {
            Thread.currentThread().setContextClassLoader(paramCl);
        }
    }

}