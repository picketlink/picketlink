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

package org.picketlink.idm.file.internal;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileGroup extends AbstractFileIdentityType implements Group {

    private static final long serialVersionUID = -8844796115187672706L;

    private String name;

    private Group parentGroup;

    public FileGroup() {
        
    }
    
    public FileGroup(String name, Group parent) {
        this.name = name;
        this.parentGroup = parent;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    @Override
    public String getKey() {
        return getName();
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parentGroup
     */
    public Group getParentGroup() {
        return parentGroup;
    }

    /**
     * @param parentGroup the parentGroup to set
     */
    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    @Override
    public String getId() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.internal.file.AbstractFileIdentityType#update()
     */
    @Override
    protected void update() {
        super.changeListener.updateGroups();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Group)) {
            return false;
        }

        Group other = (Group) obj;

        return other.getName() != null && this.getName() != null && other.getName().equals(this.getName());
    }
}
