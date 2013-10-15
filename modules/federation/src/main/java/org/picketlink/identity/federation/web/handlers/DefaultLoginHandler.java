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
package org.picketlink.identity.federation.web.handlers;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.web.interfaces.ILoginHandler;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Default LoginHandler that uses a properties file in the classpath called as users.properties whose format is
 * username=password
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 18, 2009
 */
public class DefaultLoginHandler implements ILoginHandler {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private static Properties props = new Properties();

    static {
        try {
            URL url = SecurityActions.loadResource(DefaultLoginHandler.class, "users.properties");
            if (url == null)
                throw new RuntimeException(logger.resourceNotFound("users.properties"));
            props.load(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticate(String username, Object credential) throws LoginException {
        String pass = null;
        if (credential instanceof byte[]) {
            pass = new String((byte[]) credential);
        } else if (credential instanceof String) {
            pass = (String) credential;
        } else
            throw logger.unknowCredentialType(credential.getClass().getName());

        String storedPass = (String) props.get(username);
        return storedPass != null ? storedPass.equals(pass) : false;
    }

}