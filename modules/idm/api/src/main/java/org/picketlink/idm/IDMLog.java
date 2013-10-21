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

package org.picketlink.idm;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.picketlink.common.util.logging.Log;
import org.picketlink.common.util.logging.LogFactory;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author Pedro Silva
 *
 */
@MessageLogger(projectCode = "PLIDM")
public interface IDMLog extends Log {

    /**
     * <p>This is the root logger. General messages should be logged using it.</p>
     */
    IDMLog ROOT_LOGGER = LogFactory.getLog(IDMLog.class, IDMLog.class.getPackage().getName());

    /**
     * <p>This is the root logger for identity stores. General and implementation agnostic messages should be logged using it.</p>
     */
    IDMLog IDENTITY_STORE_LOGGER = LogFactory.getLog(IDMLog.class, IDMLog.class.getPackage().getName() + ".identity.store");

    // General logging messages. Ids 1000-1099.
    @LogMessage(level = Level.INFO)
    @Message(id = 1000, value = "Bootstrapping PicketLink IDM Partition Manager")
    void partitionManagerBootstrap();

    @LogMessage(level = Level.INFO)
    @Message(id = 1001, value = "Initializing Identity Store [%s]")
    void storeInitializing(Class<? extends IdentityStore> storeType);
}