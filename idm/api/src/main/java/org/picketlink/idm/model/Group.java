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

/**
 * Group representation
 */
public interface Group extends IdentityType {
    String KEY_PREFIX = "GROUP://";

    // TODO: Javadocs
    // TODO: Exceptions

    // TODO: getId() -> getPath()? Should it stick to natural Id(path) or have non meaningful one

    // Self related

    /**
     * Groups are stored in tree hierarchy and therefore ID represents a path. ID string always begins with "/" element that
     * represents root of the tree
     * <p/>
     * Example: Valid IDs are "/acme/departments/marketing", "/security/administrator" or "/administrator". Where "acme",
     * "departments", "marketing", "security" and "administrator" are group names.
     *
     * @return Group Id in String representation.
     */
    String getId();

    /**
     * Group name is unique identifier in specific group tree branch. For example group with id "/acme/departments/marketing"
     * will have name "marketing" and parent group of id "/acme/departments"
     *
     * @return name
     */
    String getName();

    // Sub groups

    /**
     * @return parent group or null if it refers to root ("/") in a group tree.
     */
    Group getParentGroup();

}
