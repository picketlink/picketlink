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

import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.spi.SecurityContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentHandler extends IdentityTypeHandler<Agent>{

    public AgentHandler() {
        getSortParametersMapping().put(Agent.LOGIN_NAME, PropertyType.AGENT_LOGIN_NAME);
    }

    @Override
    protected void doPopulateIdentityInstance(SecurityContext context, Object toIdentity, Agent fromUser, JPAIdentityStore store) {
        JPAIdentityStoreConfiguration jpaConfig = store.getConfig();

        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupAndCreatePartitionObject(
                context, context.getPartition()), true);
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
    protected Agent doCreateIdentityType(SecurityContext context, Object identity, JPAIdentityStore store) {
        String loginName = store.getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getValue(identity).toString();

        Agent agent = new SimpleAgent(loginName);

        return agent;
    }

    @Override
    public List<Predicate> getPredicate(SecurityContext context, JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(context, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(Agent.LOGIN_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getName()),
                    parameterValues[0]));
        }

        return predicates;
    }

}
