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

    private final STSClientPool stsClientPool;

    private STSClientFactory() {
        stsClientPool = new STSClientPool();
    }

    private static class LazySTSClientFactory {
        private static final STSClientFactory INSTANCE = new STSClientFactory();
    }

    public static STSClientFactory getInstance() {
        return LazySTSClientFactory.INSTANCE;
    }

    public static STSClientFactory getInstance(int maxClientsInPool) {
        LazySTSClientFactory.INSTANCE.stsClientPool.initializePool(maxClientsInPool);
        return LazySTSClientFactory.INSTANCE;
    }

    /**
     * This method creates STS client directly without pooling based on config.
     *
     * Recommended method to use instead is createPool(final STSClientConfig config).
     *
     * @param config
     * @return
     */
    @Deprecated
    public STSClient create(final STSClientConfig config) {
        return createPool(0, config);
    }

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     *
     * When pooling is disabled it just creates client and return it.
     *
     * @param config to construct the pool of clients
     * @return STSClient from new pool
     */
    public STSClient createPool(final STSClientConfig config) {
        return createPool(0, config);
    }

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it just creates client and return it.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param config to construct the pool of clients
     * @return STSClient from new pool
     */
    public STSClient createPool(int initialNumberOfClients, final STSClientConfig config) {
        if (stsClientPool.isPoolingDisabled()) {
            return new STSClient(config);
        }
        stsClientPool.initialize(initialNumberOfClients, config);
        return stsClientPool.takeOut(config);
    }

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it just creates client and return it.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param callBack which provide configuration
     * @return STSClient from new pool
     */

    public STSClient createPool(int initialNumberOfClients, final STSClientCreationCallBack callBack) {
        if (stsClientPool.isPoolingDisabled()) {
            return callBack.createClient();
        }
        stsClientPool.initialize(initialNumberOfClients, callBack);
        return stsClientPool.takeOut(callBack.getKey());
    }

    /**
     * Returns STS client back to the sub pool of clients.
     *
     * @param stsClient client to return
     */
    public void returnClient(final STSClient stsClient) {
        if (stsClientPool.isPoolingDisabled() == false) {
            stsClientPool.putIn(stsClient);
        }
    }

    public STSClient getClient(final STSClientConfig config) {
        if (stsClientPool.isPoolingDisabled()) {
            return new STSClient(config);
        }
        return stsClientPool.takeOut(config);
    }

    public boolean configExists(final STSClientConfig config) {
        return stsClientPool.isConfigInitialized(config);
    }
}
