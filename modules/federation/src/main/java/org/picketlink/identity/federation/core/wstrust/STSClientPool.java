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

import java.util.ArrayList;
import java.util.Hashtable;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

/**
 * Simple pool of {@link STSClient} classes.
 * This class is not intended to be used directly by user code. Use {@link STSClientFactory} class instead.
 *
 * @author Peter Skopek : pskopek at (redhat.com)
 *
 */
class STSClientPool {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    static final int DEFAULT_NUM_STS_CLIENTS = 10;

    private int maxPoolSize = -1;

    private Hashtable<String, ArrayList<STSClient>> free = new Hashtable<String, ArrayList<STSClient>>();
    private Hashtable<String, ArrayList<STSClient>> inUse = new Hashtable<String, ArrayList<STSClient>>();
    private Hashtable<String, STSConfigData> configs = new Hashtable<String, STSConfigData>();

    STSClientPool() {
    }

    synchronized void initializePool(int maxPoolSize) {
        if (maxPoolSize < 0) {
            throw logger.cannotSetMaxPoolSizeToNegative(String.valueOf(maxPoolSize));
        }
        if (this.maxPoolSize == -1) {
            this.maxPoolSize = maxPoolSize;
        }
    }

    void initialize(int numberOfSTSClients, STSClientConfig stsClientConfig) {
        internalInitialize(numberOfSTSClients, stsClientConfig, null);
    }

    void initialize(STSClientConfig stsClientConfig) {
        internalInitialize(0, stsClientConfig, null);
    }


    void initialize(int numberOfSTSClients, STSClientCreationCallBack clientCreationCallBack) {
        internalInitialize(numberOfSTSClients, null, clientCreationCallBack);
    }

    private synchronized void internalInitialize(int numberOfSTSClients, STSClientConfig stsClientConfig, STSClientCreationCallBack clientCreationCallBack) {

        if (isPoolingDisabled()) {
            return;
        }

        int initSTSClients = (numberOfSTSClients > 0 ? numberOfSTSClients : DEFAULT_NUM_STS_CLIENTS);
        int numOfallClients = numberOfAllClients();

        if (numOfallClients >= maxPoolSize) {
            throw logger.maximumNumberOfClientsReachedforPool(String.valueOf(maxPoolSize));
        }

        if (maxPoolSize - initSTSClients < 0) {
            initSTSClients = maxPoolSize - numOfallClients;
        }

        String key = null;
        if (clientCreationCallBack != null) {
            key = clientCreationCallBack.getKey();
        } else {
            key = key(stsClientConfig);
        }

        if (!configs.containsKey(key)) {
            ArrayList<STSClient> clients = new ArrayList<STSClient>(initSTSClients);
            if (clientCreationCallBack != null) {
                for (int i = 0; i < initSTSClients; i++) {
                    clients.add(clientCreationCallBack.createClient());
                }
            } else {
                for (int i = 0; i < initSTSClients; i++) {
                    clients.add(new STSClient(stsClientConfig));
                }
            }
            STSConfigData configData = new STSConfigData();
            configData.initialNumberOfClients = initSTSClients;
            if (clientCreationCallBack != null) {
                configData.config = null;
                configData.callBack = clientCreationCallBack;
            } else {
                configData.config = stsClientConfig;
                configData.callBack = null;
            }
            configs.put(key, configData);
            free.put(key, clients);
            inUse.put(key, new ArrayList<STSClient>(initSTSClients));
        } else {
            // free pool already contains given key:
            throw logger.freePoolAlreadyContainsGivenKey(key);
        }

    }

    synchronized void destroy(STSClientConfig stsClientConfig) {
        String key = key(stsClientConfig);
        free.remove(key);
        inUse.remove(key);
        configs.remove(key);
    }

    STSClient takeOut(STSClientConfig stsClientConfig) {
        if (isPoolingDisabled()) {
            return new STSClient(stsClientConfig);
        }
        String key = key(stsClientConfig);
        STSClient client = takeOutInternal(key);
        if (client == null) {
            STSConfigData configData = configs.get(key);
            if (configData == null) {
                throw logger.cannotGetSTSConfigByKey(key);
            }
            initialize(configData.initialNumberOfClients, stsClientConfig);
            client = takeOutInternal(key);
        }
        return client;
    }


    STSClient takeOut(String key) {
        if (isPoolingDisabled()) {
            return null;
        }
        STSClient client = takeOutInternal(key);
        if (client == null) {
            STSConfigData configData = configs.get(key);
            if (configData == null) {
                throw logger.cannotGetSTSConfigByKey(key);
            }
            if (configData.callBack != null) {
                internalInitialize(DEFAULT_NUM_STS_CLIENTS, null, configs.get(key).callBack);
            }
            client = takeOutInternal(key);
        }
        return client;
    }

    boolean isConfigInitialized(STSClientConfig stsClientConfig) {
        if (isPoolingDisabled() == false || stsClientConfig == null) {
            return false;
         }
        STSConfigData configData = configs.get(key(stsClientConfig));
        return (configData != null);
    }

    boolean isConfigInitialized(String key) {
        if (isPoolingDisabled() == false || key == null) {
           return false;
        }
        STSConfigData configData = configs.get(key);
        return (configData != null);
    }

    void putIn(STSClientConfigKeyProvider keyProvider, STSClient client) {
        if (isPoolingDisabled() == false) {
            String key = keyProvider.getSTSClientConfigKey();
            putInInternal(key, client);
        }
    }

    void putIn(String key, STSClient client) {
        if (isPoolingDisabled() == false) {
            putInInternal(key, client);
        }
    }

    void putIn(STSClient client) {
        if (isPoolingDisabled() == false) {
            putInInternal(client.getSTSClientConfigKey(), client);
        }
    }

    private synchronized STSClient takeOutInternal(String key) {
        ArrayList<STSClient> clients = free.get(key);
        if (clients != null) {
            int size = clients.size();
            STSClient client;
            if (size > 0) {
                client = clients.remove(size - 1);
            } else {
                int allocateClients = numClientsToAllocate(key);
                if (allocateClients > 0) {
                    addClients(key);
                } else {
                    // Pool reached miximum number of clients within the pool
                    throw logger.maximumNumberOfClientsReachedforPool(String.valueOf(this.maxPoolSize));
                }
                client = clients.remove(clients.size() -1);
            }
            markInUse(key, client);
            return client;
        }
        return null;
    }

    private int numberOfAllClients() {
        return free.size() + inUse.size();
    }

    private int numClientsToAllocate(String key) {
        int allClients = 0;
        ArrayList<STSClient> clients = inUse.get(key);
        if (clients != null) {
            allClients += clients.size();
        }
        clients = free.get(key);
        if (clients != null) {
            allClients += clients.size();
        }

        int c = maxPoolSize - allClients;

        int initSTSCLients;
        STSConfigData configData = configs.get(key);
        if (configData != null) {
            initSTSCLients = configData.initialNumberOfClients;
        } else {
            initSTSCLients = DEFAULT_NUM_STS_CLIENTS;
        }

        if (c > initSTSCLients) {
            return initSTSCLients;
        } else if (c <= 0) {
            return 0;
        } else {
            return c;
        }
    }

    private void addClients(String key) {
        STSConfigData configData = configs.get(key);
        if (configData != null) {
            ArrayList<STSClient> freeClientPool = free.get(key);
            if (freeClientPool != null) {
                ArrayList<STSClient> clients = new ArrayList<STSClient>(configData.initialNumberOfClients);
                if (configData.config != null) {
                    for (int i = 0; i < configData.initialNumberOfClients; i++) {
                        clients.add(new STSClient(configData.config));
                    }
                } else {
                    for (int i = 0; i < configData.initialNumberOfClients; i++) {
                        clients.add(configData.callBack.createClient());
                    }
                }
                freeClientPool.addAll(clients);
            } else {
                // cannot get free client pool key:
                throw logger.cannotGetFreeClientPoolKey(key);
            }
        }  else {
            // cannot get STS config by key:
            throw logger.cannotGetSTSConfigByKey(key);
        }
    }

    private void markInUse(String key, STSClient client) {
        ArrayList<STSClient> usedClients = inUse.get(key);
        if (usedClients != null) {
            usedClients.add(client);
        } else {
            // cannot get used clients by key:
            logger.cannotGetUsedClientsByKey(key);
        }
    }

    private synchronized void putInInternal(String key, STSClient client) {
        ArrayList<STSClient> freeClients = free.get(key);
        ArrayList<STSClient> usedClients = inUse.get(key);

        if (!usedClients.remove(client)) {
            // removing non existing client from used clients by key:
            throw logger.removingNonExistingClientFromUsedClientsByKey(key);
        }

        freeClients.add(client);

    }

    private String key(STSClientConfig stsClientConfig) {
        return stsClientConfig.getSTSClientConfigKey();
    }

    boolean isPoolingDisabled() {
        return maxPoolSize == 0 || maxPoolSize == -1;
    }

}

class STSConfigData {
    STSClientConfig config;
    STSClientCreationCallBack callBack;
    int initialNumberOfClients = STSClientPool.DEFAULT_NUM_STS_CLIENTS;
}
