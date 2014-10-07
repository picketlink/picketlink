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
 * Interface to simple pool of {@link STSClient}s.
 *
 * @author <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 */
public interface STSClientPool {

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     *
     * When pooling is disabled it does nothing.
     *
     * @param config to construct the pool of clients
     */
    void createPool(final STSClientConfig config);

    /**
     * This method initializes sub pool of clients by given configuration data and returns client from that pool.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it does nothing.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param config to construct the pool of clients
     */
    void createPool(int initialNumberOfClients, final STSClientConfig config);

    /**
     * This method initializes sub pool of clients by given configuration data.
     * initialNumberOfClients is used to initialize the pool for the given number of clients.
     *
     * When pooling is disabled it does nothing.
     *
     * @param initialNumberOfClients initial number of clients in the pool
     * @param callBack which provide configuration
     */

    void createPool(int initialNumberOfClients, final STSClientCreationCallBack callBack);

    /**
     * Destroys client sub pool denoted by given config.
     *
     * @param config {@link STSClientConfiguration} to find client sub pool to destroy
     */
    void destroyPool(final STSClientConfig config);

    /**
     * Destroy all the pools attached to given module name.
     *
     * @param moduleName module name to destroy pools or "" or null to destroy default module's pools.
     */
    void destroyPool(final String moduleName);

    /**
     * Returns given {@link STSClient} back to the sub pool of clients.
     * Sub pool is determined automatically from client configuration.
     *
     * @param {@link STSClient} to return back to the sub pool of clients
     */
    void returnClient(final STSClient stsClient);

    /**
     * Get STSClient from sub pool denoted by config.
     * @param config {@link STSClientConfiguration} to find client sub pool
     * @return {@link STSClient} from the sub pool of clients
     */
    STSClient getClient(final STSClientConfig config);

    /**
     * Checks whether given config has already sub pool of clients created.
     *
     * @param config {@link STSClientConfiguration} to find client sub pool
     * @return true if config was already used as sub pool key
     */
    boolean configExists(final STSClientConfig config);

}
