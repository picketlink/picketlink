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

    private STSClientPool stsClientPool;

    private STSClientFactory() {
        stsClientPool = new STSClientPool();
    }

    private static class LazySTSClientFactory {
        private static final STSClientFactory INSTANCE = new STSClientFactory();
    }

    /**
     * Get instance of {@link STSClientFactory}.
     *
     * For the first time use getInstance(maxClientsInPool) to initialize the pool.
     *
     * @return {@link STSClientFactory} instance
     */
    public static STSClientFactory getInstance() {
        return LazySTSClientFactory.INSTANCE;
    }

    /**
     * Get instance of {@link STSClientFactory} and initialize underlying pool.
     *
     * Subsequent calls to this method simply return already initialize instance with no change to poll max number of clients.
     *
     * @param maxClientsInPool maximum number of {@link STSClients}s to be able to store in the pool.
     * @return {@link STSClientFactory} instance
     */
    public static STSClientFactory getInstance(int maxClientsInPool) {
        LazySTSClientFactory.INSTANCE.stsClientPool.initializePool(maxClientsInPool);
        return LazySTSClientFactory.INSTANCE;
    }

    /**
     * This method creates STS client directly without pooling based on STSClient config.
     *
     * Recommended method to use instead is getClient(final STSClientConfig config).
     *
     * @param config
     * @return STSClient
     */
    @Deprecated
    public STSClient create(final STSClientConfig config) {
        return new STSClient(config);
    }

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     *
     * When pooling is disabled it does nothing.
     *
     * @param config to construct the pool of clients
     */
    public void createPool(final STSClientConfig config) {
        createPool(0, config);
    }

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it does nothing.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param config to construct the pool of clients
     */
    public void createPool(int initialNumberOfClients, final STSClientConfig config) {
        if (stsClientPool.isPoolingDisabled() == false) {
            stsClientPool.initialize(initialNumberOfClients, config);
        }
    }

    /**
     * This method initializes sub pool of clients by given configuration data.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it does nothing.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param callBack which provide configuration
     */

    public void createPool(int initialNumberOfClients, final STSClientCreationCallBack callBack) {
        if (stsClientPool.isPoolingDisabled() == false) {
            stsClientPool.initialize(initialNumberOfClients, callBack);
        }
    }

    /**
     * Destroys client sub pool denoted by given config.
     *
     * @param config {@link STSClientConfiguration} to find client sub pool to destroy
     */
    public void destroyPool(final STSClientConfig config) {
        if (stsClientPool.isPoolingDisabled() == false) {
            stsClientPool.destroy(config);
        }
    }

    /**
     * Returns given {@link STSClient} back to the sub pool of clients.
     * Sub pool is determined automatically from client configuration.
     *
     * @param {@link STSClient} to return back to the sub pool of clients
     */
    public void returnClient(final STSClient stsClient) {
        if (stsClientPool.isPoolingDisabled() == false) {
            stsClientPool.putIn(stsClient);
        }
    }

    /**
     * Get STSClient from sub pool denoted by config.
     * @param config {@link STSClientConfiguration} to find client sub pool
     * @return {@link STSClient} from the sub pool of clients
     */
    public STSClient getClient(final STSClientConfig config) {
        if (stsClientPool.isPoolingDisabled()) {
            return new STSClient(config);
        }
        return stsClientPool.takeOut(config);
    }

    /**
     * Checks whether given config has already sub pool of clients created.
     *
     * @param config {@link STSClientConfiguration} to find client sub pool
     * @return true if config was already used as sub pool key
     */
    public boolean configExists(final STSClientConfig config) {
        return stsClientPool.isConfigInitialized(config);
    }


    /**
     * Throw away {@link STSClientPool} managed by this factory and create new default one.
     * Use STSClientFactory.getInstance(int) to initialize new {@link STSClientPool}.
     *
     * <p><b>Use with caution: this method will throw away all client sub pools</b>
     */
    public void resetFactory() {
        this.stsClientPool = new STSClientPool();
    }

}
