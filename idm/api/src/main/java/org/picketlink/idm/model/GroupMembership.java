package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.QueryParameter;

/**
 * A Relationship that represents an identity's membership in a Group
 * 
 * @author Shane Bryzak
 */
public interface GroupMembership extends Relationship {
    QueryParameter MEMBER = new QueryParameter() {};

    QueryParameter GROUP = new QueryParameter() {};

    @RelationshipIdentity
    IdentityType getMember();

    @RelationshipIdentity
    Group getGroup();
}
