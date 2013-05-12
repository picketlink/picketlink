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

import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;

/**
 * <p>
 * An <code>OperationNotSupportedException</code> is thrown to indicate that a {@link FeatureGroup} or {@link FeatureOperation}
 * is not supported by the underlying IdentityStore configured for a specific IdentityManager instance.
 * </p>
 * <p>
 * You should check the {@link IdentityStoreConfiguration} for individual features supported by a IdentityStore.
 * </p>
 *
 * @author Pedro Silva
 *
 */
public class OperationNotSupportedException extends SecurityConfigurationException {

    private static final long serialVersionUID = -669582364091679894L;

    private FeatureGroup featureGroup;
    private FeatureOperation featureOperation;

    public OperationNotSupportedException(String message, FeatureGroup feature, FeatureOperation operation) {
        super(message);
        this.featureGroup = feature;
        this.featureOperation = operation;
    }

    public FeatureGroup getFeatureGroup() {
        return this.featureGroup;
    }

    public FeatureOperation getFeatureOperation() {
        return this.featureOperation;
    }


}
