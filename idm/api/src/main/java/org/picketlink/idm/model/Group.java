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
package org.picketlink.idm.model;

import java.io.Serializable;

import org.picketlink.idm.query.QueryParameter;

/**
 * Group representation
 */
public interface Group extends IdentityType, Serializable {

    /**
     * A query parameter used to set the name value.
     */
    QueryParameter NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the path.
     */
    QueryParameter PATH = new QueryParameter() {};

    /**
     * A query parameter used to set the parent value.
     */
    QueryParameter PARENT = new QueryParameter() {};

    /**
     * Group name is unique identifier in specific group tree branch. For example group with id "/acme/departments/marketing"
     * will have name "marketing" and parent group of id "/acme/departments"
     *
     * @return name
     */
    String getName();

    /**
     * @return parent group or null if it refers to root ("/") in a group tree.
     */
    Group getParentGroup();

    /**
     * <p>Sets the parent group.</p>
     *
     * @param group
     */
    void setParentGroup(Group group);

    /**
     * @return group path (eg.: /parentGroup/childGroup.
     */
    String getPath();

}
