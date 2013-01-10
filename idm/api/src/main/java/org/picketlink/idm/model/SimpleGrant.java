package org.picketlink.idm.model;

/**
 * Simple implementation of Grant
 *  
 * @author Shane Bryzak
 */
public class SimpleGrant extends AbstractAttributedType implements Grant {

    private IdentityType assignee;
    private Role role;

    public SimpleGrant(IdentityType assignee, Role role) {
        this.assignee = assignee;
        this.role = role;
    }

    @Override
    public IdentityType getAssignee() {
        return assignee;
    }

    @Override
    public Role getRole() {
        return role;
    }

}
