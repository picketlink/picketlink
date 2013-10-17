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
package org.picketlink.internal.cache;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.cache.Cache;
import org.picketlink.cache.CacheManager;

/**
 * Default implementation of {@link CacheManager}
 * @author anil saldhana
 * @param <K>
 * @param <V>
 * @since May 10, 2013
 */
@SuppressWarnings("unchecked")
public class DefaultCacheManager<K, V> implements CacheManager {
    protected Map<String,Cache<K,V>> map = new HashMap<String,Cache<K,V>>();
    @Override
    public Cache<K, V> getCache(String cacheName) {
        return (Cache<K, V>) map.get(cacheName);
    }
    /**
     * Set the cache
     * @param cacheName
     * @param cache
     */
    public void setCache(String cacheName, Cache<K,V> cache){
        this.map.put(cacheName, cache);
    }
}