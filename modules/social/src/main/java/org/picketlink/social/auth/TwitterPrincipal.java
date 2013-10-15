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
package org.picketlink.social.auth;

import twitter4j.User;

import java.io.Serializable;
import java.security.Principal;

/**
 * Represents a {@link java.security.Principal} via Twitter login
 *
 * @author anil saldhana
 * @since Oct 3, 2012
 */
public class TwitterPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public TwitterPrincipal(User user) {
        name = user.getName();
    }

    @Override
    public String getName() {
        return name;
    }
}