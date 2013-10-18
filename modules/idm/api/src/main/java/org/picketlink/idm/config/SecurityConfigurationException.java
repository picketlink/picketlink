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
package org.picketlink.idm.config;

import org.picketlink.idm.IdentityManagementException;

/**
 * This exception is thrown when a problem is found with the Security API configuration
 */
public class SecurityConfigurationException extends IdentityManagementException {

    private static final long serialVersionUID = -8895836939958745981L;

    public SecurityConfigurationException() {
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(Throwable cause) {
        super(cause);
    }

}
