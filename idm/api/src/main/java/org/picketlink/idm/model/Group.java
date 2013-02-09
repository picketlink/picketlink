/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
