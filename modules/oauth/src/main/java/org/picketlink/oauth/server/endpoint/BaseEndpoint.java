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
package org.picketlink.oauth.server.endpoint;

import org.picketlink.idm.IdentityManager;
import org.picketlink.oauth.server.util.OAuthServerUtil;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for endpoints
 *
 * @author anil saldhana
 * @since Dec 12, 2012
 */
public class BaseEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(BaseEndpoint.class.getName());

    @Inject
    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    protected void setup() {
        if (context == null) {
            throw new RuntimeException("Servlet Context has not been injected");
        }
        if (identityManager == null) {
            try {
                identityManager = OAuthServerUtil.handleIdentityManager(context);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Identity Manager setup:", e);
                throw new RuntimeException(e);
            }
            if (identityManager == null) {
                throw new RuntimeException("Identity Manager has not been created");
            }
        }
    }

}