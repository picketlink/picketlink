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

package org.picketlink.config.idm;

/**
 * Type representing identityStoreInvocationContextFactory
 *
 * TODO: Move this class to config module. For now it needs to be in federation because needs to be accessible from PicketlinkType class
 * TODO: Add XML config snippet similarly like for other type classes
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
