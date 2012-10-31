package org.picketlink.idm.event;

/**
 * Bridges events between environments.
 *
 * @author Shane Bryzak
 */
public interface EventBridge {
    void raiseEvent(Object event);
}
