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
package org.picketlink.scim.model.v11;

/**
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class ServiceProviderConfiguration {
    private String[] schemas;
    private String documentationUrl;
    private AuthenticationSchemes[] authenticationSchemes;
    private SupportedAttribute etag;
    private SupportedAttribute sort;
    private SupportedAttribute xmlDataFormat;
    private SupportedAttribute changePassword;
    private SupportedAttribute patch;
    private Bulk bulk;
    private Filter filter;

    public String[] getSchemas() {
        return schemas;
    }

    public ServiceProviderConfiguration setSchemas(String[] schemas) {
        this.schemas = schemas;
        return this;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public ServiceProviderConfiguration setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
        return this;
    }

    public AuthenticationSchemes[] getAuthenticationSchemes() {
        return authenticationSchemes;
    }

    public ServiceProviderConfiguration setAuthenticationSchemes(AuthenticationSchemes[] authenticationSchmes) {
        this.authenticationSchemes = authenticationSchmes;
        return this;
    }

    public SupportedAttribute getEtag() {
        return etag;
    }

    public ServiceProviderConfiguration setEtag(SupportedAttribute etag) {
        this.etag = etag;
        return this;
    }

    public SupportedAttribute getSort() {
        return sort;
    }

    public ServiceProviderConfiguration setSort(SupportedAttribute sort) {
        this.sort = sort;
        return this;
    }

    public SupportedAttribute getXmlDataFormat() {
        return xmlDataFormat;
    }

    public ServiceProviderConfiguration setXmlDataFormat(SupportedAttribute xmlDataFormat) {
        this.xmlDataFormat = xmlDataFormat;
        return this;
    }

    public SupportedAttribute getChangePassword() {
        return changePassword;
    }

    public ServiceProviderConfiguration setChangePassword(SupportedAttribute changePassword) {
        this.changePassword = changePassword;
        return this;
    }

    public SupportedAttribute getPatch() {
        return patch;
    }

    public ServiceProviderConfiguration setPatch(SupportedAttribute patch) {
        this.patch = patch;
        return this;
    }

    public Bulk getBulk() {
        return bulk;
    }

    public ServiceProviderConfiguration setBulk(Bulk bulk) {
        this.bulk = bulk;
        return this;
    }

    public Filter getFilter() {
        return filter;
    }

    public ServiceProviderConfiguration setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public static class Bulk extends SupportedAttribute {
        private int maxOperations;
        private int maxPayloadSize;

        public int getMaxOperations() {
            return maxOperations;
        }

        public Bulk setMaxOperations(int maxOperations) {
            this.maxOperations = maxOperations;
            return this;
        }

        public int getMaxPayloadSize() {
            return maxPayloadSize;
        }

        public Bulk setMaxPayloadSize(int maxPayloadSize) {
            this.maxPayloadSize = maxPayloadSize;
            return this;
        }
    }

    public static class Filter extends SupportedAttribute {
        private int maxResults;

        public int getMaxResults() {
            return maxResults;
        }

        public Filter setMaxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }
    }

    public static class AuthenticationSchemes {

        private String name, description, specUrl, documentationUrl, type;
        private boolean primary;

        public String getName() {
            return name;
        }

        public AuthenticationSchemes setName(String name) {
            this.name = name;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public AuthenticationSchemes setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getSpecUrl() {
            return specUrl;
        }

        public AuthenticationSchemes setSpecUrl(String specUrl) {
            this.specUrl = specUrl;
            return this;
        }

        public String getDocumentationUrl() {
            return documentationUrl;
        }

        public AuthenticationSchemes setDocumentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public String getType() {
            return type;
        }

        public AuthenticationSchemes setType(String type) {
            this.type = type;
            return this;
        }

        public boolean isPrimary() {
            return primary;
        }

        public AuthenticationSchemes setPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }
    }
}