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
package org.picketlink.cache;

/**
 * Manager to manage cache instances
 * @author anil saldhana
 * @param <K>
 * @since May 10, 2013
 */
public interface CacheManager {
    /**
     * Given a name for the cache, return an instance of {@link Cache}
     * @param cacheName
     * @return
     */
    <K,V> Cache<K,V> getCache(String cacheName);
}