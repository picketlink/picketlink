package org.picketlink.idm.event;

import org.picketlink.idm.model.Agent;

/**
 * This event is raised when a {@link Agent} is updated
 * 
 * <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class AgentUpdatedEvent extends AbstractBaseEvent {
    
    private Agent agent;

    public AgentUpdatedEvent(Agent agent) {
        this.agent = agent;
    }

    public Agent getAgent() {
        return agent;
    }
}
