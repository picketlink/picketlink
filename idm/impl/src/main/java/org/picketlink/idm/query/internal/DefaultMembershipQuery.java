package org.picketlink.idm.query.internal;

import java.util.List;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultMembershipQuery extends AbstractQuery<DefaultMembershipQuery> implements MembershipQuery {

    private Group group;
    private Role role;
    private User user;
    private IdentityStore store;

    public DefaultMembershipQuery(IdentityStore identityStore) {
        this.store = identityStore;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public MembershipQuery setGroup(Group group) {
        this.group = group;
        return this;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public MembershipQuery setRole(Role role) {
        this.role = role;
        return this;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public MembershipQuery setUser(User user) {
        this.user = user;
        return this;
    }

    @Override
    public MembershipQuery immutable() {
        return super.getImmutable();
    }

    @Override
    public List<Membership> executeQuery(MembershipQuery query) {
        return this.store.executeQuery(getInvocationContext(store), query, null);
    }

    @Override
    public MembershipQuery setUser(String user) {
        this.user = new SimpleUser(user);
        return this;
    }

    @Override
    public MembershipQuery setGroup(String groupId) {
        this.group = new SimpleGroup(groupId, null, null);
        return this;
    }

    @Override
    public MembershipQuery setRole(String role) {
        this.role = new SimpleRole(role);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.idm.query.MembershipQuery#executeQuery()
     */
    @Override
    public List<Membership> executeQuery() {
        return this.store.executeQuery(getInvocationContext(store), this, null);
    }

}