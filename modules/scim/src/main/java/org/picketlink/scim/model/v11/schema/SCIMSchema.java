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
package org.picketlink.scim.model.v11.schema;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.annotations.ResourceDefinition;
import org.picketlink.scim.model.v11.SCIMMetaData;
import org.picketlink.scim.model.v11.resource.SCIMResource;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * SCIM SchemaElement Type
 *
 * @author Giriraj Sharma
 * @since Oct 1, 2014
 */
public class SCIMSchema {

    private String id;
    private String name;
    private String description;
    private Attribute[] attributes;
    private SCIMMetaData meta;

    public String getId() {
        return id;
    }

    public SCIMSchema setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SCIMSchema setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SCIMSchema setDescription(String description) {
        this.description = description;
        return this;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public SCIMSchema setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
        return this;
    }

    public SCIMMetaData getMeta() {
        return meta;
    }

    public SCIMSchema setMeta(SCIMMetaData meta) {
        this.meta = meta;
        return this;
    }

    public static SCIMSchema fromResourceType(Class<? extends SCIMResource> resourceType) {
        ResourceDefinition resourceDefinition = resourceType.getAnnotation(ResourceDefinition.class);

        if (resourceDefinition != null) {
            SCIMSchema scimSchema = new SCIMSchema();

            scimSchema.setId(resourceDefinition.schema());
            scimSchema.setName(resourceDefinition.name());
            scimSchema.setDescription(resourceDefinition.description());

            List<BasicAttribute> attributes = new ArrayList<BasicAttribute>();
            List<Property<Object>> result = PropertyQueries.createQuery(resourceType)
                    .addCriteria(new AnnotatedPropertyCriteria(ResourceAttributeDefinition.class)).getResultList();

            for (Property property : result) {
                attributes.add(Attribute.fromAttributeDefinition(property));
            }

            scimSchema.setAttributes(attributes.toArray(new Attribute[attributes.size()]));

            return scimSchema;
        }

        return null;
    }

    public static class Attribute extends BasicAttribute {

        public static Attribute fromAttributeDefinition(Property property) {
            ResourceAttributeDefinition annotation = property.getAnnotatedElement().getAnnotation(
                    ResourceAttributeDefinition.class);
            Attribute attribute = new Attribute();

            String name = annotation.name();

            if (isNullOrEmpty(name)) {
                name = property.getName();
            }

            attribute.setName(name);
            attribute.setDescription(annotation.description());
            attribute.setMultiValued(annotation.multiValued());
            attribute.setCaseExact(annotation.caseExact());
            attribute.setMutability(annotation.mutability());
            attribute.setRequired(annotation.required());
            attribute.setReturned(annotation.returned());
            attribute.setUniqueness(annotation.uniqueness());

            Class javaType = property.getJavaClass();
            attribute.setType(Attribute.Type.fromJavaType(javaType));

            if ("complex".equals(attribute.getType())) {
                ArrayList<BasicAttribute> subAttributes = new ArrayList<BasicAttribute>();

                if (javaType.isArray()) {
                    javaType = javaType.getComponentType();
                }

                List<Property<Object>> result = PropertyQueries.createQuery(javaType)
                        .addCriteria(new AnnotatedPropertyCriteria(ResourceAttributeDefinition.class))
                        .getResultList();

                for (Property subAttributeProperty : result) {
                    subAttributes.add(fromSubAttributeDefinition(subAttributeProperty));
                }

                attribute.setSubAttributes(subAttributes.toArray(new BasicAttribute[subAttributes.size()]));
            }

            return attribute;
        }

        public static BasicAttribute fromSubAttributeDefinition(Property property) {
            ResourceAttributeDefinition annotation = property.getAnnotatedElement().getAnnotation(
                    ResourceAttributeDefinition.class);
            Attribute attribute = new Attribute();

            String name = annotation.name();

            if (isNullOrEmpty(name)) {
                name = property.getName();
            }

            attribute.setName(name);
            attribute.setDescription(annotation.description());
            attribute.setMultiValued(annotation.multiValued());
            attribute.setCaseExact(annotation.caseExact());
            attribute.setMutability(annotation.mutability());
            attribute.setCanonicalValues(annotation.canonicalValues());
            attribute.setRequired(annotation.required());
            attribute.setReturned(annotation.returned());
            attribute.setUniqueness(annotation.uniqueness());

            Class javaType = property.getJavaClass();
            attribute.setType(Attribute.Type.fromJavaType(javaType));

            return attribute;
        }

        public static enum Type {

            STRING(String.class, "string");

            private Class javaType;
            private String type;

            Type(Class javaType, String type) {
                this.javaType = javaType;
                this.type = type;
            }

            public Class getJavaType() {
                return javaType;
            }

            public String getType() {
                return type;
            }

            public static String fromJavaType(Class javaType) {
                for (Type type : values()) {
                    if (javaType.equals(type.getJavaType())) {
                        return type.getType();
                    }
                }

                if (!javaType.isPrimitive()) {
                    return "complex";
                }

                return "undefined";
            }
        }

        private BasicAttribute[] subAttributes;

        public BasicAttribute[] getSubAttributes() {
            return subAttributes;
        }

        public Attribute setSubAttributes(BasicAttribute[] subAttributes) {
            this.subAttributes = subAttributes;
            return this;
        }
    }

    public static class BasicAttribute {

        private String name;
        private String type;
        private String description;
        private boolean multiValued;
        private boolean required;
        private boolean caseExact;
        private String[] canonicalValues;
        private String mutability;
        private String returned;
        private String uniqueness;

        public String getName() {
            return name;
        }

        public BasicAttribute setName(String name) {
            this.name = name;
            return this;
        }

        public String getType() {
            return type;
        }

        public BasicAttribute setType(String type) {
            this.type = type;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public BasicAttribute setDescription(String description) {
            this.description = description;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public BasicAttribute setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isCaseExact() {
            return caseExact;
        }

        public BasicAttribute setCaseExact(boolean caseExact) {
            this.caseExact = caseExact;
            return this;
        }

        public String[] getCanonicalValues() {
            return canonicalValues;
        }

        public BasicAttribute setCanonicalValues(String[] canonicalValues) {
            this.canonicalValues = canonicalValues;
            return this;
        }

        public boolean isMultiValued() {
            return multiValued;
        }

        public BasicAttribute setMultiValued(boolean multiValued) {
            this.multiValued = multiValued;
            return this;
        }

        public String getMutability() {
            return mutability;
        }

        public BasicAttribute setMutability(String mutability) {
            this.mutability = mutability;
            return this;
        }

        public String getReturned() {
            return returned;
        }

        public BasicAttribute setReturned(String returned) {
            this.returned = returned;
            return this;
        }

        public String getUniqueness() {
            return uniqueness;
        }

        public BasicAttribute setUniqueness(String uniqueness) {
            this.uniqueness = uniqueness;
            return this;
        }
    }
}
