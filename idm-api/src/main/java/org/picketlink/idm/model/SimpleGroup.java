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
 * Simple implementation of the Group interface
 *
 */
public class SimpleGroup extends AbstractIdentityType implements Group {
    private String id;
    private String name;
    private Group parentGroup;

    public SimpleGroup(String id, String name, Group parentGroup) {
        this.id = id;
        this.name = name;
        this.parentGroup = parentGroup;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Group getParentGroup() {
        return parentGroup;
    }

    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, id);
    }
}
