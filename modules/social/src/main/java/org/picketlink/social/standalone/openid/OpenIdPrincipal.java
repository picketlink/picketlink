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
package org.picketlink.social.standalone.openid;

import java.io.Serializable;
import java.net.URL;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * A {@link Principal} representing an OpenID Authenticated principal
 *
 * @author Marcel Kolsteren
 * @author Anil Saldhana
 * @since Jan 30, 2010
 */
public class OpenIdPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 4404673070085740561L;

    private String identifier;

    private URL openIdProvider;

    private Map<String, List<String>> attributes;

    public OpenIdPrincipal(String identifier, URL openIdProvider, Map<String, List<String>> attributes) {
        super();
        this.identifier = identifier;
        this.openIdProvider = openIdProvider;
        this.attributes = attributes;
    }

    public String getName() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public URL getOpenIdProvider() {
        return openIdProvider;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "OpenIdPrincipal [identifier=" + identifier + ", openIdProvider=" + openIdProvider + ", attributes="
                + attributes + "]";
    }
}