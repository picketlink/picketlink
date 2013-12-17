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
 *
 * @author Peter Skopek pskopek at (redhat.com)
 *
 */
public class STSClientPool {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    private static STSClientPool instance = null;

    private int maxPoolSize = 100;
    private int initialClients = 0;

    private Hashtable<String, ArrayList<STSClient>> free = new Hashtable<String, ArrayList<STSClient>>();
    private Hashtable<String, ArrayList<STSClient>> inUse = new Hashtable<String, ArrayList<STSClient>>();
    private Hashtable<String, STSConfigData> configs = new Hashtable<String, STSConfigData>();

    protected STSClientPool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public static STSClientPool instance(int maxPoolSize) {
        if (instance == null) {
            return new STSClientPool(maxPoolSize);
        }

        return instance;
    }

    public void initialize(int numberOfSTSClients, STSClientConfig stsClientConfig) {
        internalInitialize(numberOfSTSClients, stsClientConfig, null);
    }

    public void initialize(STSClientConfig stsClientConfig) {
        internalInitialize(initialClients, stsClientConfig, null);
    }


    public void initialize(int numberOfSTSClients, STSClientCreationCallBack clientCreationCallBack) {
        internalInitialize(numberOfSTSClients, null, clientCreationCallBack);
    }

    private void internalInitialize(int numberOfSTSClients, STSClientConfig stsClientConfig, STSClientCreationCallBack clientCreationCallBack) {

        if (initialClients < 1) {
            initialClients = numberOfSTSClients;
        }
        String key = null;
        if (clientCreationCallBack != null) {
            key = clientCreationCallBack.getKey();
        } else {
            key = key(stsClientConfig);
        }
        if (!free.containsKey(key)) {
            ArrayList<STSClient> clients = new ArrayList<STSClient>(numberOfSTSClients);
            if (clientCreationCallBack != null) {
                for (int i = 0; i < numberOfSTSClients; i++) {
                    clients.add(clientCreationCallBack.createClient());
                }
            } else {
                for (int i = 0; i < numberOfSTSClients; i++) {
                    clients.add(new STSClient(stsClientConfig));
                }
            }
            synchronized (free) {
                STSConfigData configData = new STSConfigData();
                if (clientCreationCallBack != null) {
                    configData.config = null;
                    configData.callBack = clientCreationCallBack;
                } else {
                    configData.config = stsClientConfig;
                    configData.callBack = null;
                }
                configs.put(key, configData);
                free.put(key, clients);
                inUse.put(key, new ArrayList<STSClient>(numberOfSTSClients));
            }
        } else {
            // free pool already contains given key:
            throw logger.freePoolAlreadyContainsGivenKey(key);
        }

    }


    public STSClient takeOut(String serviceName, String portName, String endPointAddress) {
        String key = key(serviceName, portName, endPointAddress);
        STSClient client = takeOutInternal(key);
        if (client == null) {
            initialize(initialClients, configs.get(key).config);
            client = takeOutInternal(key);
        }
        return client;
    }

    public STSClient takeOut(STSClientConfig stsClientConfig) {
        String key = key(stsClientConfig);
        STSClient client = takeOutInternal(key);
        if (client == null) {
            initialize(initialClients, stsClientConfig);
            client = takeOutInternal(key);
        }
        return client;
    }

    public STSClient takeOut(String key) {
        STSClient client = takeOutInternal(key);
        if (client == null) {
            if (configs.get(key).callBack != null) {
                internalInitialize(initialClients, null, configs.get(key).callBack);
            }
            client = takeOutInternal(key);
        }
        return client;
    }

    public void putIn(String serviceName, String portName, String endPointAddress, STSClient client) {
        String key = key(serviceName, portName, endPointAddress);
        putInInternal(key, client);
    }

    public void putIn(STSClientConfig stsClientConfig, STSClient client) {
        String key = key(stsClientConfig);
        putInInternal(key, client);
    }

    public void putIn(String key, STSClient client) {
        putInInternal(key, client);
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

        if (c > initialClients) {
            return initialClients;
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
                ArrayList<STSClient> clients = new ArrayList<STSClient>(initialClients);
                if (configData.config != null) {
                    for (int i = 0; i < initialClients; i++) {
                        clients.add(new STSClient(configData.config));
                    }
                } else {
                    for (int i = 0; i < initialClients; i++) {
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
        return key(stsClientConfig.getServiceName(), stsClientConfig.getPortName(), stsClientConfig.getEndPointAddress());
    }

    private String key(String serviceName, String portName, String endPointAddress) {
        return serviceName + "|" + portName + "|" + endPointAddress;
    }

    public boolean isPoolingDisabled() {
        return maxPoolSize == 0;
    }

}

class STSConfigData {
    STSClientConfig config;
    STSClientCreationCallBack callBack;
}
