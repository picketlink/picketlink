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
import java.util.Map;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.User;

/**
 * @author Pedro Silva
 * 
 */
public class FileAgent extends AbstractIdentityTypeEntry<Agent> {

    private static final long serialVersionUID = 1L;

    private static final transient String FILE_AGENT_VERSION = "1";

    public FileAgent(Agent agent) {
        super(FILE_AGENT_VERSION, agent);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);
        
        Agent agent = getEntry();
        
        properties.put("loginName", agent.getLoginName());
        
        if (User.class.isInstance(agent)) {
            User user = (User) agent;
            
            properties.put("firstName", user.getFirstName());
            properties.put("lastName", user.getLastName());
            properties.put("email", user.getEmail());
        }
    }

    @Override
    protected Agent doCreateInstance(Map<String, Serializable> properties) throws Exception {
        String loginName = properties.get("loginName").toString(); 
        return (Agent) Class.forName(getType()).getConstructor(String.class).newInstance(loginName);
    }
    
    @Override
    protected Agent doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        Agent agent = super.doPopulateEntry(properties);
        
        if (User.class.isInstance(agent)) {
            User user = (User) agent;

            Serializable firstName = properties.get("firstName");
            
            if (firstName != null) {
                user.setFirstName(firstName.toString());                
            }

            Serializable lastName = properties.get("lastName");
            
            if (lastName != null) {
                user.setLastName(lastName.toString());                
            }

            Serializable email = properties.get("email");
            
            if (email != null) {
                user.setEmail(email.toString());                
            }
        }
        
        return agent;
    }
}
