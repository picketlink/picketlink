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
package org.picketlink.test.idm.token;

import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;

import java.util.Date;
import java.util.List;

import static org.picketlink.idm.model.annotation.StereotypeProperty.Property;

/**
 * @author Pedro Igor
 */
public abstract class AbstractTokenConsumer<T extends AbstractSimpleToken> implements Token.Consumer<T> {

    @Override
    public <I extends IdentityType> I extractIdentity(T token, Class<I> identityType, Property stereotypeProperty, Object identifier) {
        if (Property.IDENTITY_USER_NAME.equals(stereotypeProperty)) {
            String subject = token.getSubject();

            if (identifier.equals(subject) || token.getUserName().equals(identifier)) {
                try {
                    I identityTypeInstance = Reflections.newInstance(identityType);

                    identityTypeInstance.setId(identifier.toString());

                    List<org.picketlink.common.properties.Property<Object>> properties = PropertyQueries
                        .createQuery(identityType)
                        .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                        .getResultList();

                    for (org.picketlink.common.properties.Property property : properties) {
                        StereotypeProperty annotation = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

                        if (annotation.value().equals(stereotypeProperty)) {
                            property.setValue(identityTypeInstance, identifier);
                        }
                    }

                    return identityTypeInstance;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return null;
    }

    @Override
    public boolean validate(T token) {
        if (token == null) {
            return false;
        }

        if (token.getClaims().length != 5) {
            return false;
        }

        return new Date().before(token.getExpiration());
    }

    protected abstract Class<? extends TokenCredentialStorage> getCredentialStorageType();
}
