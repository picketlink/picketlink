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
package org.picketlink.config.federation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * The claims processors specify the classes that are capable of processing specific claims dialects.
 *
 *
 * <p>
 * Java class for ClaimsProcessorsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ClaimsProcessorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ClaimsProcessor" type="{urn:picketlink:identity-federation:config:1.0}ClaimsProcessorType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ClaimsProcessorsType {

    protected List<ClaimsProcessorType> claimsProcessor = new ArrayList<ClaimsProcessorType>();

    public void add(ClaimsProcessorType claim) {
        this.claimsProcessor.add(claim);
    }

    public void remove(ClaimsProcessorType claim) {
        this.claimsProcessor.remove(claim);
    }

    /**
     * Gets the value of the claimsProcessor property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ClaimsProcessorType }
     *
     *
     */
    public List<ClaimsProcessorType> getClaimsProcessor() {
        return Collections.unmodifiableList(this.claimsProcessor);
    }

}