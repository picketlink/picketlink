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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.picketlink.common.exceptions.ParsingException;

/**
 * <p>
 * Interface to indicate the parser supports a particular namespace.
 * </p>
 *
 * <p>
 * This class needs to be moved to the security common project.
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 5, 2010
 */
public interface ParserNamespaceSupport {
    /**
     * Parse the event stream
     *
     * @param xmlEventReader
     * @return
     * @throws ParsingException
     */
    Object parse(XMLEventReader xmlEventReader) throws ParsingException;

    /**
     * Returns whether the parser supports parsing a particular namespace
     *
     * @param qname
     * @return
     */
    boolean supports(QName qname);
}