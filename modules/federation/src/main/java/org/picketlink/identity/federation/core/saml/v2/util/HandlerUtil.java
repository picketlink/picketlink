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
package org.picketlink.identity.federation.core.saml.v2.util;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deals with SAML2 Handlers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class HandlerUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static Set<SAML2Handler> getHandlers(Handlers handlers) throws ConfigurationException {
        if (handlers == null)
            throw logger.nullArgumentError("handlers");
        List<Handler> handlerList = handlers.getHandler();

        Set<SAML2Handler> handlerSet = new LinkedHashSet<SAML2Handler>();

        for (Handler handler : handlerList) {
            String clazzName = handler.getClazz();

            Class<?> clazz;
            try {
                clazz = SecurityActions.loadClass(HandlerUtil.class, clazzName);
                if (clazz == null)
                    throw new RuntimeException(logger.classNotLoadedError(clazzName));
                SAML2Handler samlhandler = (SAML2Handler) clazz.newInstance();
                List<KeyValueType> options = handler.getOption();

                Map<String, Object> mapOptions = new HashMap<String, Object>();

                for (KeyValueType kvtype : options) {
                    mapOptions.put(kvtype.getKey(), kvtype.getValue());
                }
                SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
                handlerConfig.set(mapOptions);

                samlhandler.initHandlerConfig(handlerConfig);

                handlerSet.add(samlhandler);
            } catch (InstantiationException e) {
                throw logger.configurationError(e);
            } catch (IllegalAccessException e) {
                throw logger.configurationError(e);
            }
        }
        return handlerSet;
    }
}