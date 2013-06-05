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

package org.picketlink.idm.credential.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.spi.IdentityStore;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * This factory is responsible for returning CredentialHandler instances given a specific
 * type of credentials and store
 *
 * @author Shane Bryzak
 */
@SuppressWarnings("rawtypes")
public class CredentialHandlerFactory {

    private Map<Class<? extends CredentialHandler>, CredentialHandler> handlerInstances =
            new HashMap<Class<? extends CredentialHandler>, CredentialHandler>();

    /**
     * @param credentialsClass
     * @param identityStore
     * @return
     */
    public CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass,
                                                    IdentityStore<?> identityStore) {
        return getCredentialHandler(credentialsClass, identityStore);
    }

    /**
     * @param credentialClass
     * @param identityStore
     * @return
     */
    public CredentialHandler getCredentialUpdater(Class<?> credentialClass,
                                                  IdentityStore<?> identityStore) {
        return getCredentialHandler(credentialClass, identityStore);
    }

    private CredentialHandler getCredentialHandler(Class<?> credentialsClass, IdentityStore<?> identityStore) {
        List<Class<? extends CredentialHandler>> handlers = getHandlersForStore(identityStore);

        CredentialHandler handlerInstance = null;

        for (Class<? extends CredentialHandler> handlerClass : handlers) {
            SupportsCredentials sc = handlerClass.getAnnotation(SupportsCredentials.class);

            if (sc == null) {
                throw MESSAGES.credentialSupportedCredentialsNotProvided(handlerClass);
            }

            for (Class<?> cls : sc.value()) {
                if (cls.isAssignableFrom(credentialsClass)) {
                    handlerInstance = createHandlerInstance(handlerClass, identityStore);

                    // if we found a specific handler for the credential, immediately return.
                    if (cls.equals(credentialsClass)) {
                        return handlerInstance;
                    }
                }
            }
        }

        return handlerInstance;
    }

    private List<Class<? extends CredentialHandler>> getHandlersForStore(IdentityStore<?> identityStore) {
        CredentialHandlers annotatedHandlers = identityStore.getClass().getAnnotation(CredentialHandlers.class);

        List<Class<? extends CredentialHandler>> handlers = new ArrayList<Class<? extends CredentialHandler>>(Arrays.asList(annotatedHandlers.value()));

        List<Class<? extends CredentialHandler>> customHandlers = identityStore.getConfig().getCredentialHandlers();

        if (customHandlers != null) {
            handlers.addAll(customHandlers);
        }

        return handlers;
    }

    private synchronized CredentialHandler createHandlerInstance(Class<? extends CredentialHandler> handlerClass
            , IdentityStore<?> identityStore) {
        if (!handlerInstances.containsKey(handlerClass)) {
            try {
                CredentialHandler handler = handlerClass.newInstance();

                handler.setup(identityStore);

                handlerInstances.put(handlerClass, handler);
            } catch (Exception ex) {
                throw MESSAGES.credentialCredentialHandlerInstantiationError(handlerClass, ex);
            }
        }

        return handlerInstances.get(handlerClass);
    }

}
