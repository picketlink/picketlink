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

import java.util.Set;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.picketlink.idm.config.IdentityStoreConfiguration;

/**
 * @author Pedro Silva
 * 
 */
@MessageLogger(projectCode = "PLIDM")
public interface IDMLogger extends BasicLogger {

    IDMLogger LOGGER = Logger.getMessageLogger(IDMLogger.class, IDMLogger.class.getPackage().getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 100, value = "Bootstrapping PicketLink Identity Manager")
    void identityManagerBootstrapping();

    @LogMessage(level = Level.INFO)
    @Message(id = 101, value = "Initializing Identity Store Configuration [%s] for Realms [%s]")
    void identityManagerInitConfigForRealms(IdentityStoreConfiguration config, Set<String> realms);

    @LogMessage(level = Level.INFO)
    @Message(id = 102, value = "Using working directory [%s].")
    void fileConfigUsingWorkingDir(String path);

    @LogMessage(level = Level.WARN)
    @Message(id = 103, value = "Working directory [%s] is marked to be always created. All your existing data will be lost.")
    void fileConfigAlwaysCreateWorkingDir(String path);

}