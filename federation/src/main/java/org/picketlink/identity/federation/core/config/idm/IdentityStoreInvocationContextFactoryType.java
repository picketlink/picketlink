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

package org.picketlink.identity.federation.core.config.idm;

/**
 * Type representing identityStoreInvocationContextFactory
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdentityStoreInvocationContextFactoryType {

    private String className;
    private String entityManagerFactoryClass;
    private String eventBridgeClass;
    private String credentialHandlerFactoryClass;
    private String identityCacheClass;
    private String idGeneratorClass;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEntityManagerFactoryClass() {
        return entityManagerFactoryClass;
    }

    public void setEntityManagerFactoryClass(String entityManagerFactoryClass) {
        this.entityManagerFactoryClass = entityManagerFactoryClass;
    }

    public String getEventBridgeClass() {
        return eventBridgeClass;
    }

    public void setEventBridgeClass(String eventBridgeClass) {
        this.eventBridgeClass = eventBridgeClass;
    }

    public String getCredentialHandlerFactoryClass() {
        return credentialHandlerFactoryClass;
    }

    public void setCredentialHandlerFactoryClass(String credentialHandlerFactoryClass) {
        this.credentialHandlerFactoryClass = credentialHandlerFactoryClass;
    }

    public String getIdentityCacheClass() {
        return identityCacheClass;
    }

    public void setIdentityCacheClass(String identityCacheClass) {
        this.identityCacheClass = identityCacheClass;
    }

    public String getIdGeneratorClass() {
        return idGeneratorClass;
    }

    public void setIdGeneratorClass(String idGeneratorClass) {
        this.idGeneratorClass = idGeneratorClass;
    }
}
