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

package org.picketlink.idm.file.internal;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;

/**
 * <p>
 * {@link Serializable} class used to store {@link Relationship} metadata.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileRelationship extends AbstractAttributedTypeEntry<Relationship> {

    private static final long serialVersionUID = -507972683694827934L;

    private static final transient String FILE_RELATIONSHIP_VERSION = "1";

    private Map<String, String> identityTypeIds = new HashMap<String, String>();

    protected FileRelationship(Relationship object) {
        super(FILE_RELATIONSHIP_VERSION, object);
    }

    @Override
    protected Relationship doCreateInstance(Map<String, Serializable> properties) throws Exception {
        return (Relationship) Class.forName(getType()).newInstance();
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);

        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType> createQuery(getEntry().getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

        for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
            IdentityType identityType = annotatedProperty.getValue(getEntry());
            
            if (identityType != null) {
                this.identityTypeIds.put(identityType.getId(), annotatedProperty.getName());
            }
        }
        
        List<Property<Serializable>> relationshipAttributeTypes = PropertyQueries
                .<Serializable> createQuery(getEntry().getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipAttribute.class)).getResultList();
        
        for (Property<Serializable> property : relationshipAttributeTypes) {
            properties.put(property.getName(), property.getValue(getEntry()));
        }
    }

    @Override
    protected void doWriteObject(ObjectOutputStream s) throws Exception {
        s.writeObject(this.identityTypeIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doReadObject(ObjectInputStream s) throws Exception {
        this.identityTypeIds = (Map<String, String>) s.readObject();
    }
    
    @Override
    protected Relationship doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        Relationship relationship = super.doPopulateEntry(properties);
        
        List<Property<Serializable>> relationshipAttributeTypes = PropertyQueries
                .<Serializable> createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipAttribute.class)).getResultList();
        
        for (Property<Serializable> property : relationshipAttributeTypes) {
            property.setValue(relationship, properties.get(property.getName()));
        }
        
        return relationship;
    }

    public String getIdentityTypeId(String roleName) {
        Set<Entry<String, String>> entrySet = this.identityTypeIds.entrySet();

        for (Entry<String, String> entry : entrySet) {
            if (roleName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean hasIdentityType(String id) {
        return this.identityTypeIds.containsKey(id);
    }
}