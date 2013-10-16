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
package org.picketlink.identity.federation.web.managers;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * A default attribute manager for web applications
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 10, 2009
 */
public class WebAttributeManager implements AttributeManager {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * @see AttributeManager#getAttributes(Principal, List)
     */
    public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
        throw logger.notImplementedYet("getAttributes");
    }
}