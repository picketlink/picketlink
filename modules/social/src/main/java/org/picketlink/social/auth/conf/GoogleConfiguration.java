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
 * Configuration for Google+ Login
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface GoogleConfiguration extends CommonConfiguration {

    /**
     * Get the accessType for Google+ . Valid values are "online" or "offline"
     *
     * @return accessType
     */
    String getAccessType();

    /**
     * Get the application name registered on Google+ . Value is not important (actually it could be any value)
     *
     * @return applicationName
     */
    String getApplicationName();

    /**
     * Get random algorithm, which will be used to generate "state" parameters (prevention from CSRF attacks)
     *
     * TODO: Move to parent class as it's not specific to Google+
     *
     * @return name of random algorithm
     */
    String getRandomAlgorithm();
}
