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
package org.picketlink.identity.federation.saml.v2.ac.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A type that contains a list of objects
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 3, 2011
 */
public class ObjectListType {
    protected List<Object> content = new ArrayList<Object>();

    public void add(Object obj) {
        this.content.add(obj);
    }

    public void remove(Object obj) {
        this.content.remove(obj);
    }

    public List<Object> getContent() {

        return Collections.unmodifiableList(this.content);
    }
}
