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
package org.picketlink.idm.jpa.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.picketlink.common.properties.Property;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.AbstractModel;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.AttributeMapping;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.ModelDefinition;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.PropertyMapping;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EntityGraph {

    protected class Node {

        private final Object entity;
        private final boolean singular;

        private Node owner;
        private boolean dirty;

        public Node(Object entity, boolean singular) {
            this.entity = entity;
            this.singular = singular;
        }

        public Node getOwner() {
            return owner;
        }

        public void setOwner(Node owner) {
            this.owner = owner;
        }

        public Object getEntity() {
            return entity;
        }

        public boolean isSingular() {
            return singular;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty() {
            dirty = true;
        }
    }

    private Set<Node> nodes = new HashSet<Node>();

    private EntityGraph() {}

    @SuppressWarnings("rawtypes")
    public static EntityGraph create(Object value, AbstractModel<?> model) {
        Class modelClass = value.getClass();
        EntityGraph graph = new EntityGraph();

        @SuppressWarnings("unchecked")
        Set<ModelDefinition> definitions = model.getDefinitions(modelClass);

        for (ModelDefinition definition : definitions) {
            for (Property<Object> identityProperty : definition.getProperties().keySet()) {
                PropertyMapping mapping = definition.getProperties().get(identityProperty);

                // If the property represents a collection
                if (mapping.getEntityClass() != null) {
                    // Create a node for each item in the collection
                    Object collectionValue = identityProperty.getValue(value);
                    if (Collection.class.isInstance(collectionValue)) {
                        Collection collection = (Collection) collectionValue;

                        for (Object item : collection) {
                            Node node = graph.createNode(item, false);
                            node.setDirty();
                        }
                    } else {
                        throw new IdentityManagementException("Cannot process non Collection value [" + collectionValue + "]" +
                                " for property [" + identityProperty.getName() + "] in class [" +
                                identityProperty.getDeclaringClass().getName() + "]");
                    }
                } else {
                    // Otherwise the property represents a single value -
                    // first we lookup the node containing the entity that should hold the value
                    Node node = graph.getNodeByEntityClass(mapping.getEntityClass());
                    if (node == null) {
                        // If the entity doesn't exist yet, we create it
                        node = graph.createNode(graph.createEntity(mapping.getEntityClass()), true);
                        node.setDirty();
                    }

                    mapping.getEntityProperty().setValue(node.getEntity(), identityProperty.getValue(value));
                }
            }
        }

        // If the value passed in was an AttributedType, persist the attributes
        if (value instanceof AttributedType) {
            AttributedType attributedType = (AttributedType) value;

            // For each attribute, look up the corresponding attribute for the attribute's class
            for (Attribute attribute : attributedType.getAttributes()) {
                inner: for (ModelDefinition definition : definitions) {
                    for (Class<?> supportedClass : definition.getAttributes().keySet()) {
                        // Found an attribute mapping that supports the attribute class
                        if (supportedClass.isInstance(attribute.getValue())) {
                            // Create a new attribute entity
                            AttributeMapping mapping = definition.getAttributes().get(supportedClass);
                            Object entity = graph.createEntity(mapping.getEntityClass());
                            mapping.getAttributeClass().setValue(entity, attribute.getValue().getClass().getName());
                            mapping.getAttributeName().setValue(entity, attribute.getName());

                            // TODO implement better attribute value marshaling than this
                            mapping.getAttributeValue().setValue(entity, attribute.getValue());
                            graph.createNode(entity, false);
                            break inner;
                        }
                    }
                }
            }
        }

        // Set owner references
        for (Node node : graph.nodes) {

        }

        return graph;
    }

    public static EntityGraph load(AbstractModel<?> model, Class<?> cls, Serializable identifier) {
        EntityGraph graph = new EntityGraph();

        return graph;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setProperty(Property property, Object value) {
        for (Node node : nodes) {
            if (property.getDeclaringClass().isInstance(node.getEntity())) {
                property.setValue(node.getEntity(), value);
                break;
            }
        }
    }

    public void update(Object value) {
        // TODO implement
    }

    public void persist(EntityManager entityManager) {
        // TODO implement
    }

    /**
     * Deletes all entity nodes in the graph
     *
     * @param entityManager
     */
    public void delete(EntityManager entityManager) {
        // TODO implement
    }

    private Node getNodeByEntityClass(Class<?> entityClass) {
        for (Node node : nodes) {
            if (node.isSingular() && entityClass.equals(node.getEntity().getClass())) {
                return node;
            }
        }
        return null;
    }

    public Object getEntityByType(Class<?> entityClass) {
        for (Node node : nodes) {
            if (entityClass.isInstance(node.getEntity())) {
                return node.getEntity();
            }
        }
        return null;
    }

    public Object createEntity(Class<?> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass may not be null");
        }

        try {
            return entityClass.newInstance();
        } catch (Exception ex) {
            throw new IdentityManagementException("Error creating entity class [" + entityClass.getName() + "]", ex);
        }
    }

    public Node createNode(Object entity, boolean singular) {
        Node node = new Node(entity, singular);
        nodes.add(node);
        return node;
    }
}
