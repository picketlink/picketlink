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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.IdentityLocator;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import static org.picketlink.idm.internal.ContextualIdentityManager.IDENTITY_MANAGER_CTX_PARAMETER;

/**
 * @author pedroigor
 */
public abstract class AbstractCredentialHandler<S extends IdentityStore<?>,V extends AbstractBaseCredentials,U>
        implements CredentialHandler<S, V, U> {

    protected Agent getAgent(IdentityContext context, String loginName) {
        IdentityManager identityManager = context.getParameter(IDENTITY_MANAGER_CTX_PARAMETER);

        if (identityManager == null) {
            throw new IdentityManagementException("IdentityManager not set into context.");
        }

        Agent agent = IdentityLocator.getAgent(identityManager, loginName);

        if (agent == null) {
            agent = IdentityLocator.getUser(identityManager, loginName);
        }

        return agent;
    }

}
