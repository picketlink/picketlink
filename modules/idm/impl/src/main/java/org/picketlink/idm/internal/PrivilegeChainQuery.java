package org.picketlink.idm.internal;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

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
    private final Map<Class<? extends Relationship>,Map<Property<IdentityType>,Property<IdentityType>>> privilegeChains =
            new HashMap<Class<? extends Relationship>, Map<Property<IdentityType>,Property<IdentityType>>>();

    public void registerRelationshipType(Class<? extends Relationship> relationshipType) {
        if (!privilegeChains.containsKey(relationshipType)) {
            List<Property<IdentityType>> properties = PropertyQueries.<IdentityType>createQuery(relationshipType)
                    .addCriteria(new AnnotatedPropertyCriteria(InheritsPrivileges.class))
                    .addCriteria(new TypedPropertyCriteria(IdentityType.class, TypedPropertyCriteria.MatchOption.ALL))
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

    /**
     * <p>Checks if the given <code>identity</code> inherits the privileges assigned to the given <code>assignee</code>.</p>
     *
     * @param relationshipManager
     * @param identity
     * @param assignee
     *
     * @return
     */
    public boolean inheritsPrivileges(RelationshipManager relationshipManager, IdentityType identity, IdentityType assignee) {
        if (identity == null) {
            throw MESSAGES.nullArgument("identity");
        }

        if (assignee == null) {
            throw MESSAGES.nullArgument("assignee");
        }

        // Find all of the relationships that the identity participates in, that have one or
        // more declared privilege assignments
        RelationshipQuery query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, identity);

        boolean hasPrivileges = false;

        for (Relationship relationship : new ArrayList<Relationship>(query.getResultList())) {
            Map<Property<IdentityType>, Property<IdentityType>> propertyPropertyMap = this.privilegeChains.get(relationship.getClass());

            if (propertyPropertyMap != null) {
                for (Property<IdentityType> identityProperty : propertyPropertyMap.keySet()) {
                    Property<IdentityType> assigneeProperty = propertyPropertyMap.get(identityProperty);

                    // only do the check if the relationship is the same type of the declaring class of the assignee property
                    if (assigneeProperty.getDeclaringClass().equals(relationship.getClass())) {
                        IdentityType relationshipAssignee = assigneeProperty.getValue(relationship);

                        // if the relationship assignee is the same as the given target assignee, we have a match.
                        if (relationshipAssignee.equals(assignee)) {
                            hasPrivileges = true;
                        } else if (!identity.equals(relationshipAssignee)) {
                            // we continue the inheritance lookup if the identity is not the same as the relationship assignee
                            hasPrivileges = inheritsPrivileges(relationshipManager, relationshipAssignee, assignee);
                        }

                        if (hasPrivileges) {
                            return true;
                        }
                    }
                }
            }
        }

        // otherwise, let's check if there is a parent-child relationship for the identity, so we can check inheritance from parent
        Property<IdentityType> parentProperty = PropertyQueries
            .<IdentityType>createQuery(identity.getClass())
                .addCriteria(new TypedPropertyCriteria(identity.getClass(), TypedPropertyCriteria.MatchOption.SUB_TYPE))
                .getFirstResult();

        if (parentProperty != null) {
            IdentityType parentIdentity = parentProperty.getValue(identity);

            if (parentIdentity != null) {
                return inheritsPrivileges(relationshipManager, parentIdentity, assignee);
            }
        }

        return false;
    }
}
