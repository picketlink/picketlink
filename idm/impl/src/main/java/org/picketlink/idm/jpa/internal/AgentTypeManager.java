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

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ID;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentTypeManager extends IdentityTypeManager<Agent>{

    public AgentTypeManager(JPAIdentityStore store) {
        super(store);
    }

    @Override
    protected void fromIdentityType(Object toIdentity, Agent fromUser) {
        getStore().setModelProperty(toIdentity, PROPERTY_IDENTITY_ID, fromUser.getId(), true);
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
    protected Agent createIdentityType(Object identity) {
        String idValue = getConfig().getModelProperty(PROPERTY_IDENTITY_ID).getValue(identity).toString();

        Agent agent = new SimpleAgent(idValue);
        
        return agent;
    }
    
}
