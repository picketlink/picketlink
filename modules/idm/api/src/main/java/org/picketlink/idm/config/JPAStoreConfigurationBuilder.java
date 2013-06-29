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

import java.util.ArrayList;
import java.util.List;
import static org.picketlink.idm.IDMMessages.MESSAGES;


/**
 * @author Pedro Igor
 *
 */
public class JPAStoreConfigurationBuilder extends
        AbstractIdentityStoreConfigurationBuilder<JPAIdentityStoreConfiguration, JPAStoreConfigurationBuilder> {

    private Class<?> identityClass;
    private Class<?> attributeClass;
    private Class<?> credentialClass;
    private Class<?> credentialAttributeClass;
    private Class<?> relationshipClass;
    private Class<?> relationshipIdentityClass;
    private Class<?> relationshipAttributeClass;
    private Class<?> partitionClass;

    public JPAStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    public JPAStoreConfigurationBuilder identityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
        return this;
    }

    public JPAStoreConfigurationBuilder attributeClass(Class<?> attributeClass) {
        this.attributeClass = attributeClass;
        return this;
    }

    public JPAStoreConfigurationBuilder credentialClass(Class<?> credentialClass) {
        this.credentialClass = credentialClass;
        return this;
    }

    public JPAStoreConfigurationBuilder credentialAttributeClass(Class<?> credentialAttributeClass) {
        this.credentialAttributeClass = credentialAttributeClass;
        return this;
    }

    public JPAStoreConfigurationBuilder relationshipClass(Class<?> relationshipClass) {
        this.relationshipClass = relationshipClass;
        return this;
    }

    public JPAStoreConfigurationBuilder relationshipIdentityClass(Class<?> relationshipIdentityClass) {
        this.relationshipIdentityClass = relationshipIdentityClass;
        return this;
    }

    public JPAStoreConfigurationBuilder relationshipAttributeClass(Class<?> relationshipAttributeClass) {
        this.relationshipAttributeClass = relationshipAttributeClass;
        return this;
    }

    public JPAStoreConfigurationBuilder partitionClass(Class<?> partitionClass) {
        this.partitionClass = partitionClass;
        return this;
    }

    @Override
    public JPAIdentityStoreConfiguration create() {
        List<Class<?>> entityClasses = new ArrayList<Class<?>>();

        entityClasses.add(this.identityClass);
        entityClasses.add(this.attributeClass);
        entityClasses.add(this.credentialClass);
        entityClasses.add(this.credentialAttributeClass);
        entityClasses.add(this.relationshipClass);
        entityClasses.add(this.relationshipIdentityClass);
        entityClasses.add(this.relationshipAttributeClass);
        entityClasses.add(this.partitionClass);

        return new JPAIdentityStoreConfiguration(
                entityClasses,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers());
    }

    @Override
    public void validate() {
        super.validate();

        if (this.identityClass == null) {
            throw MESSAGES.jpaConfigIdentityClassNotProvided();
        }

//        if (this.partitionClass == null) {
//            throw MESSAGES.jpaConfigPartitionClassNotProvided();
//        }
    }

    @Override
    public JPAStoreConfigurationBuilder readFrom(JPAIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);
        return this;
    }
}