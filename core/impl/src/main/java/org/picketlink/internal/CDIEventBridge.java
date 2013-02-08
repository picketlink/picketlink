package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.picketlink.idm.event.EventBridge;

/**
 * Converts events raised from PicketLink IDM into CDI events
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class CDIEventBridge implements EventBridge {

    @Inject BeanManager beanManager;

    @Override
    public void raiseEvent(Object event) {
        beanManager.fireEvent(event);
    }

}
