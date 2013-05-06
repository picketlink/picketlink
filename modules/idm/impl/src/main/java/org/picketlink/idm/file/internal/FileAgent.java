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
