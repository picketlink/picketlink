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

package org.picketlink.idm.internal;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class AbstractIdentityStore<T extends IdentityStoreConfiguration> implements IdentityStore<T> {

    // Event context parameters
    public static final String EVENT_CONTEXT_USER_ENTITY = "USER_ENTITY";
    public static final String EVENT_CONTEXT_GROUP_ENTITY = "GROUP_ENTITY";
    public static final String EVENT_CONTEXT_ROLE_ENTITY = "ROLE_ENTITY";

    @Override
    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.validate(credentials, this);
    }

    @Override
    public void updateCredential(Agent agent, Object credential) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this);
    }

    @Override
    public <T extends CredentialStorage> void storeCredential(Agent agent, T storage) {
        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        if (annotatedTypes.isEmpty()) {
            throw new IdentityManagementException("Could not find any @Stored annotated method for CredentialStorage type ["
                    + storage.getClass().getName() + "].");
        } else {
            Property<Object> storedProperty = annotatedTypes.get(0);
            Object credential = storedProperty.getValue(storage);

            if (Serializable.class.isInstance(credential)) {
                Attribute<Serializable> credentialAttribute = new Attribute<Serializable>(
                        getCredentialAttributeName(storage.getClass()), (Serializable) credential);

                agent.setAttribute(credentialAttribute);

                update(agent);
            } else {
                throw new IdentityManagementException(
                        "Methods annotated with @Stored should aways return a serializable object.");
            }
        }
    }

    @Override
    public <C extends CredentialStorage> C retrieveCredential(Agent agent, Class<C> storageClass) {
        C storage = null;
        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        if (annotatedTypes.isEmpty()) {
            throw new IdentityManagementException("Could not find any @Stored annotated method for CredentialStorage type ["
                    + storageClass.getName() + "].");
        } else {
            Property<Object> storedProperty = annotatedTypes.get(0);
            Attribute<Serializable> credentialAttribute = agent.getAttribute(getCredentialAttributeName(storageClass));

            if (credentialAttribute != null) {
                try {
                    storage = storageClass.newInstance();
                } catch (Exception e) {
                    throw new IdentityManagementException("Error while creating a " + storageClass.getName()
                            + " storage instance.", e);
                }

                storedProperty.setValue(storage, credentialAttribute.getValue());
            } else {
                throw new IdentityManagementException(
                        "Methods annotated with @Stored should aways return a serializable object.");
            }
        }

        return storage;
    }

    /**
     * <p>
     * For the given {@link CredentialStorage} resolves the user attribute name where the credential is stored.
     * </p>
     * 
     * @param storage
     * @return
     */
    private <T extends CredentialStorage> String getCredentialAttributeName(Class<T> storage) {
        return storage.getName();
    }

    protected boolean isGroupType(Class<? extends IdentityType> identityType) {
        return Group.class.isAssignableFrom(identityType);
    }

    protected boolean isRoleType(Class<? extends IdentityType> identityType) {
        return Role.class.isAssignableFrom(identityType);
    }

    protected boolean isUserType(Class<? extends IdentityType> identityType) {
        return User.class.isAssignableFrom(identityType);
    }

    protected boolean isAgentType(Class<? extends IdentityType> identityType) {
        return Agent.class.isAssignableFrom(identityType);
    }

    protected IdentityManagementException throwsNotSupportedIdentityType(IdentityType identityType) {
        return new IdentityManagementException("Not supported IdentityType.");
    }

    protected IdentityManagementException createNotImplementedYetException() {
        return new IdentityManagementException("Not implemented yet.");
    }

}
