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

import java.security.KeyPair;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A Transfer Object used by {@link XMLSignatureUtil}
 *
 * @author anil saldhana
 */
public class SignatureUtilTransferObject {
    private Document documentToBeSigned;
    private KeyPair keyPair;

    private Node nextSibling;

    private String digestMethod;

    private String referenceURI;

    private String signatureMethod;

    public Document getDocumentToBeSigned() {
        return documentToBeSigned;
    }

    public void setDocumentToBeSigned(Document documentToBeSigned) {
        this.documentToBeSigned = documentToBeSigned;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public Node getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(Node nextSibling) {
        this.nextSibling = nextSibling;
    }

    public String getDigestMethod() {
        return digestMethod;
    }

    public void setDigestMethod(String digestMethod) {
        this.digestMethod = digestMethod;
    }

    public String getReferenceURI() {
        return referenceURI;
    }

    public void setReferenceURI(String referenceURI) {
        this.referenceURI = referenceURI;
    }

    public String getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
    }
}