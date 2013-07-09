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

package org.picketlink.idm.config;

import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * <p>{@link Builder} interface defining all configuration methods for identity stores.</p>
 *
 * @author Pedro Igor
 *
 */
public interface IdentityStoreConfigurationBuilder<T extends IdentityStoreConfiguration, S extends IdentityStoreConfigurationBuilder<T, S>> extends Builder<T> {

    /**
     * <p>Enables the default feature set for this configuration.</p>
     *
     * @return
     */
    S supportAllFeatures();

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    S supportType(Class<? extends AttributedType>... types);

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    S supportGlobalRelationship(Class<? extends Relationship>... types);

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    S supportSelfRelationship(Class<? extends Relationship>... types);

    /**
     * <p>Defines which type should not be supported by this configuration.</p>
     * <p>If the operation was not provided, the type should be completely removed from the supported types.
     * Otherwise, only the provided operations should not be supported.</p>
     *
     * @param type
     * @param operation
     * @return
     */
    S unsupportType(Class<? extends AttributedType> type, IdentityOperation... operation);

    /**
     * <p>Adds a custom {@CredentialHandler}.</p>
     *
     * @param credentialHandler
     * @return
     */
    S addCredentialHandler(Class<? extends CredentialHandler> credentialHandler);

    /**
     * <p>Sets a configuration property for a {@link CredentialHandler}.</p>
     *
     * @param propertyName
     * @param value
     * @return
     */
    S setCredentialHandlerProperty(String propertyName, Object value);

    /**
     * <p>Adds a {@link ContextInitializer}.</p>
     *
     * @param contextInitializer
     * @return
     */
    S addContextInitializer(ContextInitializer contextInitializer);

    /**
     * <p>Enable/Disable credentials support</p>
     *
     * @param supportCredentials
     * @return
     */
    S supportCredentials(boolean supportCredentials);
}