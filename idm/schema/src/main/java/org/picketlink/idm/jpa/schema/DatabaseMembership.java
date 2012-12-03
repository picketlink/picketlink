package org.picketlink.idm.jpa.schema;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * <p>
 * JPA Entity that maps {@link GroupRole} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
@NamedQuery(name = NamedQueries.MEMBERSHIP_LOAD_BY_KEY, query = "select m from DatabaseMembership m where m.role = :role and m.member = :member and m.group = :group")
public class DatabaseMembership implements GroupRole {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private DatabaseUser member;

    @ManyToOne(cascade = CascadeType.ALL)
    private DatabaseGroup group;

    @ManyToOne(cascade = CascadeType.ALL)
    private DatabaseRole role;

    public DatabaseMembership() {

    }

    public DatabaseMembership(IdentityType member, Group group, Role role) {
        if (member instanceof User) {
            setMember((DatabaseUser) member);
        } else {
            throw new UnsupportedOperationException("Only User memberships are supported.");
        }

        setRole((DatabaseRole) role);
        setGroup((DatabaseGroup) group);
    }

    /**
     * @return
     */
    public String getId() {
        return String.valueOf(this.id);
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    /**
     * @return the user
     */
    public DatabaseUser getMember() {
        return member;
    }

    /**
     * @param user the user to set
     */
    public void setMember(DatabaseUser user) {
        this.member = user;
    }

    /**
     * @return the group
     */
    public DatabaseGroup getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(DatabaseGroup group) {
        this.group = group;
    }

    /**
     * @return the role
     */
    public DatabaseRole getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(DatabaseRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DatabaseMembership)) {
            return false;
        }

        DatabaseMembership other = (DatabaseMembership) obj;

        return new EqualsBuilder().append(getId(), other.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("role", getRole()).append("group", getGroup())
                .append("user", getMember()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }
}
