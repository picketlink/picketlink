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
package org.picketlink.social.standalone.openid.providers.helpers;

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerAssociationStore;
import org.openid4java.server.ServerManager;

/**
 * Manages a OpenID Provider
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 15, 2009
 */
public class OpenIDProviderManager {
    /**
     * Internal server manager for processing
     */
    private ServerManager serverManager = new ServerManager();

    /**
     * Initialize internal data structures
     */
    public void initialize() {
        serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
        serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
    }

    /**
     * Initialize the Shared Association and Private Association stores
     *
     * @param sharedAssociationStore a set of 2 association stores {@code ServerAssociationStore}
     * @throws {@code IllegalArgumentException} if the number of stores is not 2
     */
    public void initialize(ServerAssociationStore... sharedAssociationStore) {
        if (sharedAssociationStore == null || sharedAssociationStore.length == 0) {
            initialize();
            return;
        }

        if (sharedAssociationStore.length != 2)
            throw new IllegalArgumentException("Number of association stores not equal to 2");
        serverManager.setSharedAssociations(sharedAssociationStore[0]);
        serverManager.setPrivateAssociations(sharedAssociationStore[1]);
    }

    /**
     * Get the end point where the provider is active
     *
     * @return string an url
     */
    public String getEndPoint() {
        return serverManager.getOPEndpointUrl();
    }

    /**
     * Set the end point where the provider is active
     *
     * @param url
     */
    public void setEndPoint(String url) {
        serverManager.setOPEndpointUrl(url);
    }

    /**
     * Process a request from the RP/Relying Party (or OpenID Consumer) for authenticating an user
     *
     * @param requestParams
     * @param userSelId
     * @param userSelClaimed
     * @param authenticatedAndApproved
     * @return
     */
    public OpenIDMessage processAuthenticationRequest(ParameterList requestParams, String userSelId, String userSelClaimed,
            boolean authenticatedAndApproved) {
        Message authMessage = serverManager.authResponse(requestParams, userSelId, userSelClaimed, authenticatedAndApproved);

        return new OpenIDMessage(authMessage);
    }

    /**
     * Process a request for association from the RP
     *
     * @param requestParams
     * @return
     */
    public OpenIDMessage processAssociationRequest(ParameterList requestParams) {
        return new OpenIDMessage(serverManager.associationResponse(requestParams));
    }

    /**
     * Process a verification request from RP for an already authenticated user
     *
     * @param requestParams
     * @return
     */
    public OpenIDMessage verify(ParameterList requestParams) {
        return new OpenIDMessage(serverManager.verify(requestParams));
    }

    /**
     * Create an error message that needs to be passed to the RP
     *
     * @param msg
     * @return
     */
    public OpenIDMessage getDirectError(String msg) {
        return new OpenIDMessage(DirectError.createDirectError(msg));
    }

    /**
     * Class to hold the open id message
     */
    public static class OpenIDMessage {
        private Message message;

        OpenIDMessage(Message message) {
            this.message = message;
        }

        public boolean isSuccessful() {
            return message instanceof AuthSuccess;
        }

        public String getDestinationURL(boolean httpget) {
            return ((AuthSuccess) message).getDestinationUrl(httpget);
        }

        public String getResponseText() {
            return message.keyValueFormEncoding();
        }
    }
}