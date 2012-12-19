package org.picketlink.idm.event;

import org.picketlink.idm.model.Agent;

/**
 * This event is raised when a new {@link Agent} is created
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class AgentCreatedEvent extends AbstractBaseEvent {
    
    private Agent agent;

    public AgentCreatedEvent(Agent agent) {
        this.agent = agent;
    }

    public Agent getAgent() {
        return agent;
    }
}
