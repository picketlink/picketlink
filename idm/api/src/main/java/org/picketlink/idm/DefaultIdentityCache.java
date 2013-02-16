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

package org.picketlink.idm;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultIdentityCache implements IdentityCache {

    private Map<Partition, Map<String, Agent>> agentsCache = new HashMap<Partition, Map<String, Agent>>();
    
    @Override
    public User lookupUser(Realm realm, String loginName) {
        Map<String, Agent> agents = agentsCache.get(realm);
        Agent agent = agents.get(loginName);
        
        if (User.class.isInstance(agent)) {
            return (User) agent;
        }
        
        return null;
    }

    @Override
    public Group lookupGroup(Partition partition, String groupId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role lookupRole(Partition partition, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putUser(Realm realm, User user) {
        Map<String, Agent> agents = agentsCache.get(realm);
        
        if (agents == null) {
            agents = new HashMap<String, Agent>();
            
            agents.put(user.getLoginName(), user);
        }
        
        agentsCache.put(realm, agents);
    }

    @Override
    public void putGroup(Partition partition, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void putRole(Partition partition, Role role) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Agent lookupAgent(Realm realm, String id) {
        return null;
    }

    @Override
    public void putAgent(Realm realm, Agent agent) {
        
    }

}