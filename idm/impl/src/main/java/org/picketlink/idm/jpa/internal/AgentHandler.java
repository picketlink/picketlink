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
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentHandler extends IdentityTypeHandler<Agent>{

    public AgentHandler(JPAIdentityStoreConfiguration config) {
        super(config);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Agent fromUser, JPAIdentityStore store) {
        setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentDeletedEvent(fromIdentityType);
    }

    @Override
    protected Agent doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String loginName = getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getValue(identity).toString();

        Agent agent = new SimpleAgent(loginName);
        
        return agent;
    }
    
    @Override
    public List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {

        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        Root<?> root = criteria.getRoot();
        
        
        if (queryParameter.equals(Agent.LOGIN_NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getName()),
                    parameterValues[0]));
        }
        
        return predicates;
    }
    
    @Override
    public void onBeforeAdd(Agent identityType, IdentityStore<?> store) {
        if (store.getAgent(identityType.getLoginName()) != null) {
            throw new IdentityManagementException("An Agent already exists with the given loginName [" + identityType.getLoginName() + "]");
        }
    }
    
}
