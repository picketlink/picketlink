package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.QueryParameter;

/**
 * Represents the grant of a Role to an Assignee 
 * 
 * @author Shane Bryzak
 */
public interface Grant extends Relationship {
    QueryParameter ASSIGNEE = new QueryParameter() {};

    QueryParameter ROLE = new QueryParameter() {};

    @RelationshipIdentity
    IdentityType getAssignee();

    @RelationshipIdentity
    Role getRole();
}
