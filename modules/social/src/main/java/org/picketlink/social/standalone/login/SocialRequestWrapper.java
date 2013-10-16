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
package org.picketlink.social.standalone.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * An implementation of {@link HttpServletRequestWrapper}
 *
 * @author anil saldhana
 * @since Sep 19, 2012
 */
public class SocialRequestWrapper extends HttpServletRequestWrapper {

    protected Principal userPrincipal = null;

    protected HttpServletRequest delegate = null;

    public SocialRequestWrapper(HttpServletRequest request) {
        super(request);
        this.delegate = request;
    }

    public void setUserPrincipal(Principal principal) {
        this.userPrincipal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }
}