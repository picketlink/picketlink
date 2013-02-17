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

package org.picketlink.idm.jpa.internal;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentHandler extends IdentityTypeHandler<Agent>{

    public AgentHandler(JPAIdentityStoreConfiguration config) {
        super(config);

        getSortParametersMapping().put(Agent.LOGIN_NAME, PropertyType.AGENT_LOGIN_NAME);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Agent fromUser, JPAIdentityStore store) {
        setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupPartitionObject(store.getCurrentRealm()), true);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Agent fromIdentityType) {
        return new AgentCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Agent fromIdentityType) {
        return new AgentUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Agent fromIdentityType) {
        return new AgentDeletedEvent(fromIdentityType);
    }

    @Override
    protected Agent doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String loginName = getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getValue(identity).toString();

        Agent agent = new SimpleAgent(loginName);
        
        return agent;
    }
    
    @Override
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(Agent.LOGIN_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getName()),
                    parameterValues[0]));
        }
        
        return predicates;
    }
    
}
