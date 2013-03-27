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

package org.picketlink.idm.credential.internal;

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.spi.IdentityStore;

/**
 * A basic implementation of CredentialHandlerFactory that is pre-configured with the built-in
 * CredentialHandlers, and allows registration of additional handlers.
 *
 * @author Shane Bryzak
 */
public class DefaultCredentialHandlerFactory implements CredentialHandlerFactory {

    private Map<Class<? extends CredentialHandler>, CredentialHandler> handlerInstances =
            new HashMap<Class<? extends CredentialHandler>, CredentialHandler>();

    @Override
    public CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass,
            IdentityStore<?> identityStore) {
        List<Class<? extends CredentialHandler>> handlers = getHandlersForStore(identityStore);

        for (Class<? extends CredentialHandler> handlerClass : handlers) {
            if (handlerSupports(handlerClass, credentialsClass)) {
                if (!handlerInstances.containsKey(handlerClass)) {
                    return createHandlerInstance(handlerClass);
                } else {
                    return handlerInstances.get(handlerClass);
                }
            }
        }

        return null;
    }

    @Override
    public CredentialHandler getCredentialUpdater(Class<?> credentialClass,
            IdentityStore<?> identityStore) {
        List<Class<? extends CredentialHandler>> handlers = getHandlersForStore(identityStore);

        for (Class<? extends CredentialHandler> handlerClass : handlers) {
            if (handlerSupports(handlerClass, credentialClass)) {
                if (!handlerInstances.containsKey(handlerClass)) {
                    CredentialHandler handlerInstance = createHandlerInstance(handlerClass);

                    handlerInstance.setup(identityStore);

                    return handlerInstance;
                } else {
                    return handlerInstances.get(handlerClass);
                }
            }
        }

        return null;
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

    private synchronized CredentialHandler createHandlerInstance(Class<? extends CredentialHandler> handlerClass) {
        CredentialHandler handler = null;
        if (!handlerInstances.containsKey(handlerClass)) {
            try {
                handler = handlerClass.newInstance();
                handlerInstances.put(handlerClass, handler);
            } catch (Exception ex) {
                throw MESSAGES.credentialCredentialHandlerInstantiationError(handlerClass, ex);
            }
        } else {
            handler = handlerInstances.get(handlerClass);
        }

        return handler;
    }

    private boolean handlerSupports(Class<? extends CredentialHandler> handlerClass, Class<?> credentialClass) {
        SupportsCredentials sc = handlerClass.getAnnotation(SupportsCredentials.class);

        for (Class<?> cls : sc.value()) {
            if (cls.equals(credentialClass)) {
                return true;
            }
        }

        return false;
    }

}
