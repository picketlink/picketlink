package org.picketlink.idm.model;

/**
 * A simple representation of a group membership
 * 
 * @author Shane Bryzak
 *
 */
public class SimpleGroupMembership extends AbstractAttributedType implements GroupMembership {

    private static final long serialVersionUID = 6851576454138812116L;

    private IdentityType member;
    private Group group;

    public SimpleGroupMembership(IdentityType member, Group group) {
        this.member = member;
        this.group = group;
    }

    @Override
    public IdentityType getMember() {
        return member;
    }

    @Override
    public Group getGroup() {
        return group;
    }
}
