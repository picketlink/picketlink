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
package org.picketlink.identity.federation.ws.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 16, 2011
 */
public class SimpleAnyType implements SimpleCollectionUsage<Object> {

    protected List<Object> any = new ArrayList<Object>();

    /**
     * Gets the value of the any property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Object } {@link org.w3c.dom.Element }
     */
    public List<Object> getAny() {
        return Collections.unmodifiableList(this.any);
    }

    public void add(Object t) {
        this.any.add(t);
    }

    public boolean remove(Object t) {
        return any.remove(t);

    }
}