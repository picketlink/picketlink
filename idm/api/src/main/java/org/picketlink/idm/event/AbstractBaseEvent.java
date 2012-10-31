package org.picketlink.idm.event;

/**
 * A base class for all event class that provides an event context
 * 
 * @author Shane Bryzak
 */
public abstract class AbstractBaseEvent {
    private EventContext context = new EventContext();

    public EventContext getContext() {
        return context;
    }
}
