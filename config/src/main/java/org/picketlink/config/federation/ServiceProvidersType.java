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
 * The service providers specify the token type expected by each service provider.
 *
 *
 * <p>
 * Java class for ServiceProvidersType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ServiceProvidersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServiceProvider" type="{urn:picketlink:identity-federation:config:1.0}ServiceProviderType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ServiceProvidersType {

    protected List<ServiceProviderType> serviceProvider = new ArrayList<ServiceProviderType>();

    public void add(ServiceProviderType sp) {
        this.serviceProvider.add(sp);
    }

    public void remove(ServiceProviderType sp) {
        this.serviceProvider.remove(sp);
    }

    /**
     * Gets the value of the serviceProvider property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ServiceProviderType }
     *
     *
     */
    public List<ServiceProviderType> getServiceProvider() {
        return Collections.unmodifiableList(this.serviceProvider);
    }

}