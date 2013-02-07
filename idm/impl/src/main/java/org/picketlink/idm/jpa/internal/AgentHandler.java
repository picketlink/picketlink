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

package org.picketlink.idm.jpa.internal;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.query.QueryParameter;

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
        getConfig().setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupPartitionObject(store.getCurrentRealm()), true);
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
    
    @Override
    public void validate(Agent agent, JPAIdentityStore store) {
        if (agent.getLoginName() == null) {
            throw new IdentityManagementException("No login name was provided.");
        }
        
        if (store.getAgent(agent.getLoginName()) != null) {
            throw new IdentityManagementException("Agent already exists with the given loginName [" + agent.getLoginName() + "] for the given Realm [" + store.getCurrentRealm().getName() + "]");
        }
    }
    
}
