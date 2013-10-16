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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;

import java.util.Map;

/**
 * <p>
 * Factory class used for instantiating pluggable services, such as the {@code WSTrustRequestHandler} and
 * {@code SecurityTokenProvider} implementations.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustServiceFactory {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private static final WSTrustServiceFactory factory = new WSTrustServiceFactory();

    /**
     * <p>
     * Creates the {@code WSTrustConfigurationFactory} singleton instance.
     * </p>
     */
    private WSTrustServiceFactory() {
    }

    /**
     * <p>
     * Obtains a reference to the singleton instance.
     * </p>
     *
     * @return the {@code WSTrustConfigurationFactory} singleton.
     */
    public static WSTrustServiceFactory getInstance() {
        return factory;
    }

    /**
     * <p>
     * Constructs and returns the {@code WSTrustRequestHandler} that will be used to handle WS-Trust requests.
     * </p>
     *
     * @param configuration a reference to the {@code STSConfiguration}.
     *
     * @return a reference to the constructed {@code WSTrustRequestHandler} object.
     */
    public WSTrustRequestHandler createRequestHandler(String handlerClassName, STSConfiguration configuration) {
        try {
            Class<?> clazz = SecurityActions.loadClass(getClass(), handlerClassName);
            if (clazz == null)
                throw logger.classNotLoadedError(handlerClassName);
            WSTrustRequestHandler handler = (WSTrustRequestHandler) clazz.newInstance();
            handler.initialize(configuration);
            return handler;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Constructs and returns a {@code SecurityTokenProvider} from the specified class name.
     * </p>
     *
     * @param providerClass the FQN of the {@code SecurityTokenProvider} to be instantiated.
     * @param properties a {@code Map<String, String>} containing the properties that have been configured for the
     * token
     * provider.
     *
     * @return a reference to the constructed {@code SecurityTokenProvider} object.
     */
    public SecurityTokenProvider createTokenProvider(String providerClass, Map<String, String> properties) {
        try {
            Class<?> clazz = SecurityActions.loadClass(getClass(), providerClass);
            if (clazz == null)
                throw logger.classNotLoadedError(providerClass);
            SecurityTokenProvider tokenProvider = (SecurityTokenProvider) clazz.newInstance();
            tokenProvider.initialize(properties);
            return tokenProvider;
        } catch (Exception pae) {
            throw new RuntimeException(logger.couldNotCreateInstance(providerClass, pae));
        }
    }

    /**
     * <p>
     * Constructs and returns a {@code ClaimsProcessor} from the specified class name. The processor is initialized with
     * the
     * specified properties map.
     * </p>
     *
     * @param processorClass the FQN of the {@code ClaimsProcessor} to be instantiated.
     * @param properties a {@code Map<String, String>} containing the properties that have been configured for the
     * claims
     * processor.
     *
     * @return a reference to the constructed {@code ClaimsProcessor} object.
     */
    public ClaimsProcessor createClaimsProcessor(String processorClass, Map<String, String> properties) {
        try {
            Class<?> clazz = SecurityActions.loadClass(getClass(), processorClass);
            if (clazz == null)
                throw logger.classNotLoadedError(processorClass);
            ClaimsProcessor claimsProcessor = (ClaimsProcessor) clazz.newInstance();
            claimsProcessor.initialize(properties);
            return claimsProcessor;
        } catch (Exception pae) {
            throw new RuntimeException(logger.couldNotCreateInstance("claims processor " + processorClass, pae));
        }
    }
}