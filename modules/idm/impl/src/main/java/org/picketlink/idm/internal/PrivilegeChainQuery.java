package org.picketlink.idm.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * Stores privilege chain metadata and performs chain queries to determine privilege inheritance
 *
 * @author Shane Bryzak
 */
public class PrivilegeChainQuery {

    /**
     * A mapping between a Relationship class and a set of chains that determine privilege inheritance.  The Set contains
     * mappings between the privileged identity property and the inherited identity property.
     */
    private Map<Class<? extends Relationship>,Map<Property<IdentityType>,Property<IdentityType>>> privilegeChains =
            new HashMap<Class<? extends Relationship>, Map<Property<IdentityType>,Property<IdentityType>>>();

    public void registerRelationshipType(Class<? extends Relationship> relationshipType) {
        if (!privilegeChains.containsKey(relationshipType)) {
            List<Property<IdentityType>> properties = PropertyQueries.<IdentityType>createQuery(relationshipType)
                    .addCriteria(new AnnotatedPropertyCriteria(InheritsPrivileges.class))
                    .addCriteria(new TypedPropertyCriteria(IdentityType.class))
                    .getResultList();

            Map<Property<IdentityType>,Property<IdentityType>> inheritanceMapping =
                    new HashMap<Property<IdentityType>,Property<IdentityType>>();

            for (Property<IdentityType> p : properties) {
                InheritsPrivileges annotation = p.getAnnotatedElement().getAnnotation(InheritsPrivileges.class);
                String assigneeName = annotation.value();

                if (StringUtil.isNullOrEmpty(assigneeName)) {
                    throw new IllegalArgumentException(String.format("Specified relationshipType [%s] does not declare valid " +
                            "@InheritsPrivilege annotation on property [%s] - missing assignee property name",
                            relationshipType.getName(), p.getName()));
                }

                // Lookup the assignee property by name
                Property<IdentityType> assignee = PropertyQueries.<IdentityType>createQuery(relationshipType)
                        .addCriteria(new NamedPropertyCriteria(assigneeName))
                        .getSingleResult();

                inheritanceMapping.put(p, assignee);
            }

            privilegeChains.put(relationshipType, inheritanceMapping);
        }
    }

    public Set<Class<? extends Relationship>> getRelationshipClasses() {
        return privilegeChains.keySet();
    }

    /**
     *
     * @param identity
     * @param relationships
     * @param assignee
     * @return
     */
    public boolean inheritsPrivileges(RelationshipManager relationshipManager, IdentityType identity, IdentityType assignee) {
        // Find all of the relationships that the identity participates in, that have one or
        // more declared privilege assignments
        Set<Class<? extends Relationship>> classes = getRelationshipClasses();

        Set<Relationship> relationships = new HashSet<Relationship>();

        for (Class<? extends Relationship> relationshipClass : classes) {
            RelationshipQuery query = relationshipManager.createRelationshipQuery(relationshipClass);
            query.setParameter(Relationship.IDENTITY, identity);
            relationships.addAll(query.getResultList());
        }

        // TODO

        return false;
    }
}
