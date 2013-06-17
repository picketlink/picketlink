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
package org.picketlink.idm.jpa.annotations.entity;

/**
 * This annotation is applied to an entity bean to indicate that it is used to store attribute data
 * for identity types, relationships, credentials or partitions.  If the name property is provided, the
 * values represented by the entity will be mapped as a multi-valued attribute of the same name, for the
 * owning entity.  If the supportedClasses property is provided, then this entity will only be used to store
 * values of the types specified by supportedClasses.
 *
 * The name and supportedClasses properties are mutually exclusive - only one or the other may be specified,
 * and if both are provided an exception will be thrown during initialization of the identity store.
 *
 * @author Shane Bryzak
 */
public @interface MappedAttribute {
    String name() default "";
    Class[] supportedClasses() default {};
}
