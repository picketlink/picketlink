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


/**
 * Simple factory for creating {@link STSClient}s.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * @author <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 */
public final class STSClientFactory {

    private static final int INITIAL_NUMBER_OF_CLIENTS_IN_POOL = 10;
    private static STSClientFactory INSTANCE = null;
    private static STSClientPool POOL = null;

    private STSClientFactory() {
    }

    public static STSClientFactory getInstance() {
        if (INSTANCE == null) {
            // pooling disabled
            return getInstance(0);
        }
        return INSTANCE;
    }

    public static STSClientFactory getInstance(int maxClientsInPool) {
        if (INSTANCE == null) {
            INSTANCE = new STSClientFactory();
            POOL = STSClientPool.instance(maxClientsInPool);
        }
        return INSTANCE;
    }

    public STSClient create(final STSClientConfig config) {
        return create(INITIAL_NUMBER_OF_CLIENTS_IN_POOL, config);
    }

    public STSClient create(int initialNumberOfClients, final STSClientConfig config) {
        if (POOL.isPoolingDisabled()) {
            return new STSClient(config);
        }
        POOL.initialize(initialNumberOfClients, config);
        return POOL.takeOut(config);
    }

    public STSClient create(int initialNumberOfClients, final STSClientCreationCallBack callBack) {
        if (POOL.isPoolingDisabled()) {
            return null;
        }
        POOL.initialize(initialNumberOfClients, callBack);
        return POOL.takeOut(callBack.getKey());
    }

}
