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
package org.picketlink.idm.token.internal;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.TokenStoreConfiguration;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.credential.handler.TokenCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.annotation.RelationshipStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.Condition;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.query.internal.EqualCondition;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.Token.Consumer;
import static org.picketlink.idm.spi.IdentityContext.CREDENTIALS;

/**
 * @author Pedro Igor
 */
@CredentialHandlers({
    TokenCredentialHandler.class})
public class TokenIdentityStore extends AbstractIdentityStore<TokenStoreConfiguration>
    implements CredentialStore<TokenStoreConfiguration>,
    PartitionStore<TokenStoreConfiguration> {

    private List<Consumer> tokenConsumers;

    @Override
    public void setup(TokenStoreConfiguration config) {
        super.setup(config);
        this.tokenConsumers = config.getTokenConsumer();
    }

    @Override
    protected void removeFromRelationships(IdentityContext context, IdentityType identityType) {

    }

    @Override
    protected void removeCredentials(IdentityContext context, Account account) {

    }

    @Override
    protected void updateAttributedType(IdentityContext context, AttributedType attributedType) {

    }

    @Override
    protected void removeAttributedType(IdentityContext context, AttributedType attributedType) {

    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> query) {
        List<V> identityTypes = new ArrayList<V>();
        Class<V> identityTypeType = query.getIdentityType();
        IdentityStereotype stereotype = identityTypeType.getAnnotation(IdentityStereotype.class);

        if (stereotype == null) {
            throw new IdentityManagementException("Type [" + identityTypeType + "] does not define a " + IdentityStereotype.class + ".");
        }

        Token currentToken = getCurrentToken(context);
        IdentityType identityType = null;

        for (Condition condition : query.getConditions()) {
            QueryParameter queryParameter = condition.getParameter();
            String queryParameterName = ((AttributeParameter) queryParameter).getName();

            if (IdentityType.PARTITION.equals(queryParameter)) {
                continue;
            }

            if (EqualCondition.class.isInstance(condition)) {
                EqualCondition equalCondition = (EqualCondition) condition;
                Object queryParameterValue = equalCondition.getValue();

                if (queryParameterValue == null) {
                    throw new IdentityManagementException("Query parameter [" + queryParameterName + "] does not have any value.");
                }

                if (IdentityType.ID.equals(queryParameter)) {
                    identityType = getTokenConsumer(currentToken)
                        .extractIdentity(currentToken, identityTypeType, StereotypeProperty.Property.IDENTITY_ID, queryParameterValue);
                } else {
                    Property<Object> mappedProperty = PropertyQueries
                        .createQuery(identityTypeType)
                        .addCriteria(new NamedPropertyCriteria(queryParameterName))
                        .getFirstResult();

                    if (mappedProperty == null) {
                        throw new IdentityManagementException("IdentityType [" + identityTypeType + "] does not have a property with name [" + queryParameterName + "].");
                    }

                    StereotypeProperty stereotypeProperty = mappedProperty.getAnnotatedElement()
                        .getAnnotation(StereotypeProperty.class);

                    if (stereotypeProperty == null) {
                        throw new IdentityManagementException("Query parameter [" + queryParameterName + "] does not maps to a " + StereotypeProperty.Property.class + ".");
                    }

                    identityType = getTokenConsumer(currentToken)
                        .extractIdentity(currentToken, identityTypeType, stereotypeProperty
                            .value(), queryParameterValue);
                }
            } else {
                throw new IdentityManagementException("Unsupported query condition. Token store only understands equality condition.");
            }
        }

        if (identityType != null) {
            identityTypes.add((V) identityType);
        }

        return unmodifiableList(identityTypes);
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        ArrayList<V> relationships = new ArrayList<V>();
        Class<V> relationshipType = query.getRelationshipClass();
        RelationshipStereotype stereotype = relationshipType.getAnnotation(RelationshipStereotype.class);

        if (stereotype == null) {
            throw new IdentityManagementException("Type [" + relationshipType + "] does not define a " + RelationshipStereotype.class + ".");
        }

        V relationshipInstance = null;

        for (QueryParameter queryParameter : query.getParameters().keySet()) {
            String queryParameterName = ((RelationshipQueryParameter) queryParameter).getName();
            Property<Object> nameProperty = PropertyQueries
                .createQuery(relationshipType)
                .addCriteria(new NamedPropertyCriteria(queryParameterName))
                .getFirstResult();

            if (nameProperty == null) {
                throw new IdentityManagementException("Type [" + relationshipType + "] does not have a property with name [" + queryParameterName + "].");
            }

            StereotypeProperty stereotypeProperty = nameProperty.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

            if (stereotypeProperty == null) {
                throw new IdentityManagementException("Query parameter [" + queryParameterName + "] does not maps to a " + StereotypeProperty.Property.class + " for type [" + relationshipType + ".");
            }

            Object[] queryParameterValues = query.getParameter(queryParameter);

            if (queryParameterValues == null || queryParameterValues.length == 0) {
                throw new IdentityManagementException("Query parameter [" + queryParameterName + "] does not have any value.");
            } else if (queryParameterValues.length > 1) {
                throw new IdentityManagementException("Query parameter [" + queryParameterName + "] value must be single-valued.");
            }

            Token currentToken = getCurrentToken(context);

            if (currentToken != null) {
                IdentityType identityType = resolveIdentityTypeFromToken(currentToken, queryParameterValues, stereotypeProperty);

                if (identityType == null) {
                    return emptyList();
                }

                if (relationshipInstance == null) {
                    try {
                        relationshipInstance = Reflections.newInstance(relationshipType);
                    } catch (Exception e) {
                        throw new IdentityManagementException("Could not create Relationship type [" + relationshipType + "].");
                    }
                }

                Property property = resolveProperty(relationshipType, stereotypeProperty.value());

                property.setValue(relationshipInstance, identityType);
            }
        }

        if (relationshipInstance != null) {
            relationships.add(relationshipInstance);
        }

        return unmodifiableList(relationships);
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        return emptyList();
    }

    @Override
    public void removeCredential(IdentityContext identityContext, Account account, Class<? extends CredentialStorage> aClass) {
    }

    private IdentityType resolveIdentityTypeFromToken(Token currentToken, Object[] queryParameterValues, StereotypeProperty stereotypeProperty) {
        IdentityType identityType;

        try {
            identityType = (IdentityType) queryParameterValues[0];
        } catch (ClassCastException cce) {
            throw new IdentityManagementException("Query parameter value is not an IdentityType instance.", cce);
        }

        if (identityType == null) {
            throw new IdentityManagementException("Query parameter value can not be null.");
        }

        if (StereotypeProperty.Property.RELATIONSHIP_GRANT_ROLE.equals(stereotypeProperty.value())) {
            return extractIdentityTypeFromToken(currentToken, identityType, StereotypeProperty.Property.IDENTITY_ROLE_NAME);
        } else if (StereotypeProperty.Property.RELATIONSHIP_GRANT_ASSIGNEE.equals(stereotypeProperty.value())
            || StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER.equals(stereotypeProperty.value())) {
            return extractIdentityTypeFromToken(currentToken, identityType, StereotypeProperty.Property.IDENTITY_USER_NAME);
        } else if (StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_GROUP.equals(stereotypeProperty.value())) {
            return extractIdentityTypeFromToken(currentToken, identityType, StereotypeProperty.Property.IDENTITY_GROUP_NAME);
        }

        throw new IdentityManagementException("Could not resolve any IdentityType [" + identityType + "] from Token [" + currentToken + ".");
    }

    private IdentityType extractIdentityTypeFromToken(Token token, IdentityType identityType, StereotypeProperty.Property stereotypeProperty) {
        Property mappedProperty = resolveProperty(identityType.getClass(), stereotypeProperty);
        Object identifier = mappedProperty.getValue(identityType);

        if (identifier == null) {
            throw new IdentityManagementException("The IdentityType [" + identityType + "] does not have a value for property [" + mappedProperty
                .getName() + "].");
        }

        return getTokenConsumer(token).extractIdentity(token, identityType.getClass(), stereotypeProperty, identifier);
    }

    private Consumer getTokenConsumer(Token token) {
        for (Consumer consumer : this.tokenConsumers) {
            if (consumer.getTokenType().isAssignableFrom(token.getClass())) {
                return consumer;
            }
        }

        throw MESSAGES.credentialNoConsumerForToken(token);
    }

    private Token getCurrentToken(IdentityContext context) {
        TokenCredential tokenCredential = getAuthenticatedAccountCredentials(context);

        return tokenCredential.getToken();
    }

    private TokenCredential getAuthenticatedAccountCredentials(IdentityContext context) {
        TokenCredential tokenCredential;

        try {
            tokenCredential = context.getParameter(CREDENTIALS);
        } catch (ClassCastException cce) {
            throw new IdentityManagementException("ContextParameter [" + CREDENTIALS + " does not reference a TokenCredential type instance.");
        }

        if (tokenCredential == null) {
            throw new IdentityManagementException("No TokenCredential found in the invocation context. Make sure you have a ContextInitializer which sets it.");
        }

        return tokenCredential;
    }

    /**
     * <p>Resolves a {@link org.picketlink.common.properties.Property} from the given <code>type</code> mapped with a certain {@link
     * org.picketlink.idm.model.annotation.StereotypeProperty.Property}.</p>
     *
     * @param type The type.
     * @param stereotypeProperty The stereotype property to look for.
     *
     * @return
     *
     * @throws org.picketlink.idm.IdentityManagementException If no property exists in the given type for the given stereotype
     *                                                        property.
     */
    private Property resolveProperty(Class<?> type, StereotypeProperty.Property stereotypeProperty) throws IdentityManagementException {
        List<Property<Object>> properties = PropertyQueries
            .createQuery(type)
            .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
            .getResultList();

        if (properties.isEmpty()) {
            throw new IdentityManagementException("IdentityType [" + type + "] does not have any property mapped with " + StereotypeProperty.class + ".");
        }

        for (Property property : properties) {
            StereotypeProperty propertyStereotypeProperty = property.getAnnotatedElement().getAnnotation(StereotypeProperty.class);

            if (stereotypeProperty.equals(propertyStereotypeProperty.value())) {
                return property;
            }
        }

        throw new IdentityManagementException("Could not resolve property in type [" + type + " for StereotypeProperty [" + stereotypeProperty + ".");
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        return null;
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        return (P) new Realm(Realm.DEFAULT_REALM);
    }

    @Override
    public <P extends Partition> List<P> get(IdentityContext identityContext, Class<P> partitionClass) {
        ArrayList<P> partitions = new ArrayList<P>();

        partitions.add((P) get(identityContext, Realm.class, Realm.DEFAULT_REALM));

        return partitions;
    }

    @Override
    public <P extends Partition> P lookupById(IdentityContext context, Class<P> partitionClass, String id) {
        return null;
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {

    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {

    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {

    }
}
