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
package org.picketlink.idm.credential;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;

import java.util.List;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>Provides built-in support to extract information for a particular {@link org.picketlink.idm.model.IdentityType} from a
 * {@link org.picketlink.idm.credential.Token}.</p>
 *
 * <p>The default implementation is based on the identity stereotypes defined by the {@link org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype}.</p>
 *
 * @author Pedro Igor
 */
public abstract class AbstractTokenConsumer<T extends Token> implements Token.Consumer<T> {

    @Override
    public <I extends IdentityType> I extractIdentity(T token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (token == null || token.getToken() == null) {
            throw MESSAGES.nullArgument("Token");
        }

        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (stereotypeProperty == null) {
            throw MESSAGES.nullArgument("Identifier value");
        }

        if (identifier == null) {
            throw MESSAGES.nullArgument("Identifier value");
        }

        return extractIdentityTypeFromToken(token, identityType, stereotypeProperty, identifier);
    }

    /**
     * <p>Subclasses must override this method to extract the subject's identifier from the token.</p>
     *
     * @param token
     * @return
     */
    protected abstract String extractSubject(T token);

    /**
     * <p>Subclasses must override this method to extract roles from the token.</p>
     *
     * @param token
     * @return
     */
    protected abstract Set<String> extractRoles(T token);

    /**
     * <p>Subclasses must override this method to extract groups from the token.</p>
     *
     * @param token
     * @return
     */
    protected abstract Set<String> extractGroups(T token);

    private <I extends IdentityType> I extractIdentityTypeFromToken(T token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (hasIdentityType(token, stereotypeProperty, identifier)) {
            try {
                I identityTypeInstance = Reflections.newInstance(identityType);
                Property property = resolveProperty(identityType, stereotypeProperty);

                property.setValue(identityTypeInstance, identifier);

                if (Account.class.isAssignableFrom(identityType)) {
                    Property userNameProperty = resolveProperty(identityType, StereotypeProperty.Property.IDENTITY_USER_NAME);

                    userNameProperty.setValue(identityTypeInstance, extractSubject(token));
                }

                return identityTypeInstance;
            } catch (Exception e) {
                throw new IdentityManagementException("Could not extract IdentityType [" + identityType + "] from Token [" + token + "].", e);
            }
        }

        return null;
    }

    private Property resolveProperty(Class<? extends IdentityType> identityType, StereotypeProperty.Property stereotypeProperty) {
        List<Property<Object>> properties = PropertyQueries
            .createQuery(identityType)
            .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
            .getResultList();

        if (properties.isEmpty()) {
            throw new IdentityManagementException("IdentityType [" + identityType + "] does not have any property mapped with " + StereotypeProperty.class + ".");
        }

        for (Property property : properties) {
            StereotypeProperty propertyStereotypeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

            if (stereotypeProperty.equals(propertyStereotypeProperty.value())) {
                return property;
            }
        }

        throw new IdentityManagementException("Could not resolve property in type [" + identityType + " for StereotypeProperty [" + stereotypeProperty + ".");
    }

    private boolean hasIdentityType(T token, StereotypeProperty.Property stereotypeProperty, Object identifier) {
        if (StereotypeProperty.Property.IDENTITY_ROLE_NAME.equals(stereotypeProperty)) {
            Set<String> roleNames = extractRoles(token);

            if (roleNames.contains(identifier)) {
                return true;
            }
        }

        if (StereotypeProperty.Property.IDENTITY_GROUP_NAME.equals(stereotypeProperty)) {
            Set<String> groupNames = extractGroups(token);

            if (groupNames.contains(identifier)) {
                return true;
            }
        }

        if (StereotypeProperty.Property.IDENTITY_USER_NAME.equals(stereotypeProperty)
            || StereotypeProperty.Property.IDENTITY_ID.equals(stereotypeProperty)) {
            String subject = extractSubject(token);

            if (subject != null && identifier.equals(subject)) {
                return true;
            }
        }

        return false;
    }
}
