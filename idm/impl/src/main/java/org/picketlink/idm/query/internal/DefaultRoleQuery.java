package org.picketlink.idm.query.internal;

import java.util.List;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultRoleQuery extends AbstractQuery<DefaultRoleQuery> implements RoleQuery {

    private IdentityStore store;
    private Group group;
    private IdentityType owner;

    public DefaultRoleQuery(IdentityStore jpaIdentityStore) {
        this.store = jpaIdentityStore;
    }

    @Override
    public List<Role> executeQuery(RoleQuery query) {
        return this.store.executeQuery(getInvocationContext(store), query, null);
    }

    @Override
    public List<Role> executeQuery() {
        return this.store.executeQuery(getInvocationContext(store), this, null);
    }

    @Override
    public RoleQuery setOwner(IdentityType owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public IdentityType getOwner() {
        return this.owner;
    }

    @Override
    public RoleQuery setGroup(Group group) {
        this.group = group;
        return this;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public RoleQuery setGroup(String groupId) {
        this.group = new SimpleGroup(groupId, null, null);
        return this;
    }

}