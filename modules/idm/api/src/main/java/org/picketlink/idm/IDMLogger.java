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
import org.picketlink.idm.model.AttributedType;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

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

    @LogMessage(level = Level.ERROR)
    @Message(id = 104, value = "No suitable configuration found for type operation [%s.%s]. Could find an IdentityStore to perform the requested operation.")
    void identityManagerUnsupportedOperation(Class<? extends AttributedType> type, IdentityOperation operation);

    @LogMessage(level = Level.ERROR)
    @Message(id = 105, value = "No configuration found for the given Realm [%s].")
    void identityManagerRealmNotConfigured(String realmId);

    @LogMessage(level = Level.WARN)
    @Message(id = 106, value = "Partition features are disabled. Did you provide a JPA Entity class to store partitions ?")
    void jpaConfigDisablingPartitionFeatures();

    @LogMessage(level = Level.WARN)
    @Message(id = 107, value = "Relationship features are disabled. Did you provide a JPA Entity class to store relationships ?")
    void jpaConfigDisablingRelationshipFeatures();

    @LogMessage(level = Level.WARN)
    @Message(id = 108, value = "Credential features are disabled. Did you provide a JPA Entity class to store credentials ?")
    void jpaConfigDisablingCredentialFeatures();

    @LogMessage(level = Level.ERROR)
    @Message(id = 109, value = "No configuration found for the given Tier [%s].")
    void identityManagerTierNotConfigured(String tierId);
}