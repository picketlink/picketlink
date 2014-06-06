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

package org.picketlink.internal;

import org.picketlink.idm.event.EventBridge;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static org.picketlink.log.BaseLog.ROOT_LOGGER;

/**
 * Converts events raised from PicketLink IDM into CDI events
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class CDIEventBridge implements EventBridge {

    @Inject
    private BeanManager beanManager;

    @Override
    public void raiseEvent(Object event) {
        fireEvent(event);
    }

    public void fireEvent(Object event) {
        if (ROOT_LOGGER.isDebugEnabled()) {
            ROOT_LOGGER.debugf("Firing event [%s].", event);
        }

        this.beanManager.fireEvent(event);
    }
}
