package org.picketlink.idm.permission;

import java.io.Serializable;

import org.picketlink.idm.query.RelationshipCriteria;

/**
 * Represents a "fuzzy" resource permission that is assigned via a relationship criteria, i.e. it is granted to the
 * identities for which relationships exist that match the specified criteria.  The assigneeProperty property is
 * the property name of the matching relationship that represents the identity for which the permission is assigned.
 * Standard privilege inheritance rules apply.
 *
 * @author Shane Bryzak
 *
 */
public class FuzzyPermission extends Permission {
    private RelationshipCriteria criteria;
    private String assigneeProperty;

    public FuzzyPermission(Object resource, RelationshipCriteria criteria, String assigneeProperty, String operation) {
        super(resource, operation);
        this.criteria = criteria;
        this.assigneeProperty = assigneeProperty;
    }

    public FuzzyPermission(Class<?> resourceClass, Serializable resourceIdentifier, RelationshipCriteria criteria,
            String assigneeProperty, String operation) {
        super(resourceClass, resourceIdentifier, operation);
        this.criteria = criteria;
        this.assigneeProperty = assigneeProperty;
    }

    public RelationshipCriteria getCriteria() {
        return criteria;
    }

    public String getAssigneeProperty() {
        return assigneeProperty;
    }
}
