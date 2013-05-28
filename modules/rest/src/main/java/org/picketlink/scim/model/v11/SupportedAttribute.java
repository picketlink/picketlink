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
 * A SCIM attribute that has a flag called supported
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SupportedAttribute {
    private boolean supported;

    public boolean isSupported() {
        return supported;
    }

    public SupportedAttribute setSupported(boolean supported) {
        this.supported = supported;
        return this;
    }
}