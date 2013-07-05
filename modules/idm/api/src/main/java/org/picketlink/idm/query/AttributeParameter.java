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
package org.picketlink.idm.query;

/**
 * <p>This class can be used to define a query parameter for properties annotated with
 * {@link org.picketlink.idm.model.annotation.AttributeProperty}.
 * </p>
 *
 * @author pedroigor
 */
public class AttributeParameter implements QueryParameter {

    private final String name;

    public AttributeParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
