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
package org.picketlink.social.auth.conf;

/**
 * Configuration for Facebook Login
 * @author Anil Saldhana
 * @since May 30, 2013
 */
public interface FacebookConfiguration {
    /**
     * Get the Client ID
     * @return
     */
    public String getClientID();

    /**
     * Get the Client Secret
     * @return
     */
    public String getClientSecret();

    /**
     * Get the Scope
     * @return
     */
    public String getScope();

    /**
     * Get the Return URL
     * @return
     */
    public String getReturnURL();
}