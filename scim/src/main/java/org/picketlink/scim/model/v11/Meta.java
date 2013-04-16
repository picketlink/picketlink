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
 * Type representing Metadata about a SCIM Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class Meta {
    private String created;
    private String lastModified;
    private String location;
    private String version;
    private String[] attributes;

    public String getCreated() {
        return created;
    }

    public Meta setCreated(String created) {
        this.created = created;
        return this;
    }

    public String getLastModified() {
        return lastModified;
    }

    public Meta setLastModified(String lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Meta setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Meta setVersion(String version) {
        this.version = version;
        return this;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public Meta setAttributes(String[] attributes) {
        this.attributes = attributes;
        return this;
    }
}