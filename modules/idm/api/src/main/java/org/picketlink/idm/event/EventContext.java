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

package org.picketlink.idm.event;

import java.util.HashMap;
import java.util.Map;

/**
 * The event context may be used to pass arbitrary state to event observers
 *
 * @author Shane Bryzak
 */
public class EventContext {

    private Map<String,Object> context;

    public Object getValue(String name) {
        return context != null ? context.get(name) : null;
    }

    public void setValue(String name, Object value) {
        if (context == null) {
            context = new HashMap<String,Object>();
        }
        context.put(name, value);
    }

    public boolean contains(String name) {
        return context != null && context.containsKey(name);
    }

    public boolean isEmpty() {
        return context == null || context.isEmpty();
    }
}
