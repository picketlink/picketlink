/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.config.idm;

/**
 * Type with configuration of IDM
 *
 * TODO: Move this class to config module. For now it needs to be in federation because needs to be accessible from PicketlinkType class
 * TODO: Add XML config snippet similarly like for other type classes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMType {

    private String identityManagerClass;
    private String storeFactoryClass;
    private IdentityStoreInvocationContextFactoryType identityStoreInvocationContextFactory;

    private IdentityConfigurationType identityConfigurationType;

    public String getIdentityManagerClass() {
        return identityManagerClass;
    }

    public void setIdentityManagerClass(String identityManagerClass) {
        this.identityManagerClass = identityManagerClass;
    }

    public String getStoreFactoryClass() {
        return storeFactoryClass;
    }

    public void setStoreFactoryClass(String storeFactoryClass) {
        this.storeFactoryClass = storeFactoryClass;
    }

    public IdentityStoreInvocationContextFactoryType getIdentityStoreInvocationContextFactory() {
        return identityStoreInvocationContextFactory;
    }

    public void setIdentityStoreInvocationContextFactory(IdentityStoreInvocationContextFactoryType identityStoreInvocationContextFactory) {
        this.identityStoreInvocationContextFactory = identityStoreInvocationContextFactory;
    }

    public IdentityConfigurationType getIdentityConfigurationType() {
        return identityConfigurationType;
    }

    public void setIdentityConfigurationType(IdentityConfigurationType identityConfigurationType) {
        this.identityConfigurationType = identityConfigurationType;
    }
}
