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

package org.picketlink.common.properties.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A criteria that matches a property based on its type
 *
 * @see PropertyCriteria
 */
public class TypedPropertyCriteria implements PropertyCriteria 
{
    private final Class<?> propertyClass;
    private final boolean selectSubTypes;

    public TypedPropertyCriteria(Class<?> propertyClass) 
    {
        this(propertyClass, false);
    }

    public TypedPropertyCriteria(Class<?> propertyClass, boolean selectSubTypes)
    {
        this.propertyClass = propertyClass;
        this.selectSubTypes = selectSubTypes;
    }

    public boolean fieldMatches(Field f) 
    {
        return propertyClass.equals(f.getType());
    }

    public boolean methodMatches(Method m) 
    {
        boolean equals = propertyClass.equals(m.getReturnType());

        if (!equals && selectSubTypes) {
            equals = propertyClass.isAssignableFrom(m.getReturnType());
        }

        return equals;
    }
}
