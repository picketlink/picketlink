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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;

/**
 * @author Pedro Silva
 * 
 */
public class FilePartition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Agent> agents = new HashMap<String, Agent>();
    private Map<String, Role> roles = new HashMap<String, Role>();
    private Map<String, Group> groups = new HashMap<String, Group>();
    private Map<String, Map<String, List<FileCredentialStorage>>> credentials = new HashMap<String, Map<String, List<FileCredentialStorage>>>();
    private Map<String, List<FileRelationshipStorage>> relationships = new HashMap<String, List<FileRelationshipStorage>>();

    public Map<String, Agent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, Agent> agents) {
        this.agents = agents;
    }

    public Map<String, Role> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Role> roles) {
        this.roles = roles;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public Map<String, Map<String, List<FileCredentialStorage>>> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Map<String, List<FileCredentialStorage>>> credentials) {
        this.credentials = credentials;
    }

    public Map<String, List<FileRelationshipStorage>> getRelationships() {
        return relationships;
    }

    public void setRelationships(Map<String, List<FileRelationshipStorage>> relationships) {
        this.relationships = relationships;
    }

}
