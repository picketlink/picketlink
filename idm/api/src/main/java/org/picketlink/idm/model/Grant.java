package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipIdentity;

/**
 * Represents the grant of a Role to an Assignee 
 * 
 * @author Shane Bryzak
 */
public class Grant extends AbstractAttributedType implements Relationship {
    private static final long serialVersionUID = -200089007240264375L;

    public static final IdentityTypeQueryParameter ASSIGNEE = new IdentityTypeQueryParameter() {
        
        @Override
        public String getName() {
            return "assignee";
        }
    };;

    public static final IdentityTypeQueryParameter ROLE = new IdentityTypeQueryParameter() {
        
        @Override
        public String getName() {
            return "role";
        }
    };;

    private IdentityType assignee;
    private Role role;

    public Grant() {
        
    }
    
    public Grant(IdentityType assignee, Role role) {
        this.assignee = assignee;
        this.role = role;
    }

    @RelationshipIdentity
    public IdentityType getAssignee() {
        return assignee;
    }
    
    public void setAssignee(IdentityType assignee) {
        this.assignee = assignee;
    }

    @RelationshipIdentity
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
