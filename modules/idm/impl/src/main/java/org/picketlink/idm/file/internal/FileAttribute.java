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
package org.picketlink.idm.file.internal;

import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author pedroigor
 */
public class FileAttribute extends AbstractFileType<Collection<Attribute<? extends Serializable>>> {

    private static final String VERSION = "1";
    private String identityTypeId;

    protected FileAttribute(AttributedType identityType) {
        super(VERSION, Collections.synchronizedList(new ArrayList<Attribute<? extends Serializable>>(identityType
                .getAttributes())));
        this.identityTypeId = identityType.getId();
    }

    @Override
    protected Collection<Attribute<? extends Serializable>> doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        List<Attribute<? extends Serializable>> attributes = new ArrayList<Attribute<?extends Serializable>>();

        for (String name: properties.keySet()) {
            attributes.add(new Attribute(name, properties.get(name)));
        }

        return attributes;
    }

    @Override
    protected void doWriteObject(final ObjectOutputStream s) throws Exception {
        super.doWriteObject(s);
        s.writeObject(this.identityTypeId);
    }

    @Override
    protected void doReadObject(final ObjectInputStream s) throws Exception {
        super.doReadObject(s);
        this.identityTypeId = s.readObject().toString();
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        for (Attribute attribute: new ArrayList<Attribute>(getEntry())) {
            properties.put(attribute.getName(), attribute.getValue());
        }
    }
}
