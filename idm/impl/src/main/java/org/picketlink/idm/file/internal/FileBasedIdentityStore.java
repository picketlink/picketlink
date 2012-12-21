/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.file.internal;


import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * <p>
 * File based {@link IdentityStore} implementation. By default, each new instance recreate the data files. This behaviour can be
 * changed by configuring the <code>alwaysCreateFiles</code> property to false.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileBasedIdentityStore extends AbstractIdentityStore<FileIdentityStoreConfiguration> {

    private static final String USER_CERTIFICATE_ATTRIBUTE = "usercertificate";
    private static final String USER_PASSWORD_ATTRIBUTE = "userPassword";

    private FileIdentityStoreConfiguration config;
    private IdentityStoreInvocationContext context;
    
    @Override
    public void setup(FileIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.config;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return this.context;
    }

    /**
     * <p>
     * Flush all changes made to users to the filesystem.
     * </p>
     */
    synchronized void flushUsers() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getUsersFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getUsers());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Flush all changes made to roles to the filesystem.
     * </p>
     */
    synchronized void flushRoles() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getRolesFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getRoles());
            oos.close();
        } catch (Exception e) {
        }
    }

    /**
     * <p>
     * Flush all changes made to groups to the filesystem.
     * </p>
     */
    synchronized void flushGroups() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getGroupsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getGroups());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Flush all changes made to memberships to the filesystem.
     * </p>
     */
    synchronized void flushMemberships() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getMembershipsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getMemberships());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @Override
    public void add(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (isUserType(identityTypeClass)) {
            addUser(identityType);
        } else if (isGroupType(identityTypeClass)) {
            addGroup(identityType);
        } else if (isRoleType(identityTypeClass)) {
            addRole(identityType);
        }
    }

    private void addRole(IdentityType identityType) {
        Role role = (Role) identityType;
        SimpleRole fileRole = new SimpleRole(role.getName());

        updateCommonProperties(role, fileRole);
        
        getConfig().getRoles().put(role.getName(), fileRole);
        flushRoles();
    }

    private void addGroup(IdentityType identityType) {
        Group group = (Group) identityType;
        SimpleGroup fileGroup = null;

        if (group.getParentGroup() != null) {
            fileGroup = new SimpleGroup(group.getName(), getGroup(group.getParentGroup().getName()));
        } else {
            fileGroup = new SimpleGroup(group.getName());
        }
        
        updateCommonProperties(group, fileGroup);

        getConfig().getGroups().put(group.getName(), fileGroup);
        flushGroups();
    }

    private void addUser(IdentityType identityType) {
        User user = (User) identityType;

        SimpleUser fileUser;

        if (!(user instanceof SimpleUser)) {
            fileUser = new SimpleUser(user.getId());

            fileUser.setFirstName(user.getFirstName());
            fileUser.setLastName(user.getLastName());
            fileUser.setEmail(user.getEmail());
            
            updateCommonProperties(user, fileUser);
        } else {
            fileUser = (SimpleUser) user;
        }
        
        getConfig().getUsers().put(user.getId(), fileUser);
        flushUsers();
    }

    private void updateCommonProperties(IdentityType fromIdentityType, IdentityType toIdentityType) {
        toIdentityType.setEnabled(fromIdentityType.isEnabled());
        toIdentityType.setCreatedDate(fromIdentityType.getCreatedDate());
        toIdentityType.setExpirationDate(fromIdentityType.getExpirationDate());

        for (Object object : toIdentityType.getAttributes().toArray()) {
            Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
            toIdentityType.removeAttribute(attribute.getName());
        }

        for (Attribute<? extends Serializable> attrib : fromIdentityType.getAttributes()) {
            toIdentityType.setAttribute(attrib);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (isUserType(identityTypeClass)) {
            updateUser(identityType);
        } else if (isGroupType(identityTypeClass)) {
            updateGroup(identityType);
        } else if (isRoleType(identityTypeClass)) {
            updateRole(identityType);
        }
    }

    private void updateRole(IdentityType identityType) {
        Role role = (Role) identityType;
        SimpleRole fileRole = null;

        if (!SimpleRole.class.isInstance(role)) {
            fileRole = (SimpleRole) getRole(role.getName());
            
            updateCommonProperties(role, fileRole);
        } else {
            fileRole = (SimpleRole) role;
        }

        getConfig().getRoles().put(role.getName(), fileRole);
        flushRoles();
    }

    private void updateGroup(IdentityType identityType) {
        Group group = (Group) identityType;
        SimpleGroup fileGroup = null;

        if (!SimpleGroup.class.isInstance(group)) {
            fileGroup = (SimpleGroup) getGroup(group.getName());
            
            updateCommonProperties(group, fileGroup);
        } else {
            fileGroup = (SimpleGroup) group;
        }

        getConfig().getGroups().put(group.getName(), fileGroup);
        flushGroups();
    }

    private void updateUser(IdentityType identityType) {
        User user = (User) identityType;
        SimpleUser fileUser = null;

        if (!SimpleUser.class.isInstance(user)) {
            fileUser = (SimpleUser) getUser(user.getId());

            fileUser.setFirstName(user.getFirstName());
            fileUser.setLastName(user.getLastName());
            fileUser.setEmail(user.getEmail());
            
            updateCommonProperties(user, fileUser);
        } else {
            fileUser = (SimpleUser) user;
        }

        getConfig().getUsers().put(user.getId(), fileUser);
        flushUsers();
    }

    @Override
    public void remove(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            removeUser(identityType);
        } else if (Group.class.isInstance(identityType)) {
            removeGroup(identityType);
        } else if (Role.class.isInstance(identityType)) {
            removeRole(identityType);
        }
    }

    private void removeRole(IdentityType identityType) {
        Role role = (Role) identityType;

        getConfig().getRoles().remove(role.getName());

        for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
            IdentityType member = membership.getMember();

            if (Group.class.isInstance(member)) {
                Role roleMember = (Role) member;
                Role roleMembership = membership.getRole();

                if (roleMember.getName().equals(role.getName())
                        || (roleMembership != null && roleMembership.getName().equals(role.getName()))) {
                    getConfig().getMemberships().remove(membership);
                }
            }
        }

        flushRoles();
        flushMemberships();
    }

    private void removeGroup(IdentityType identityType) {
        Group group = (Group) identityType;

        getConfig().getGroups().remove(group.getName());

        for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
            IdentityType member = membership.getMember();

            if (Group.class.isInstance(member)) {
                Group groupMember = (Group) member;
                Group groupMembership = membership.getGroup();

                if (groupMember.getName().equals(group.getName())
                        || (groupMembership != null && groupMembership.getName().equals(group.getName()))) {
                    getConfig().getMemberships().remove(membership);
                }
            }
        }

        flushGroups();
        flushMemberships();
    }

    private void removeUser(IdentityType identityType) {
        User user = (User) identityType;

        getConfig().getUsers().remove(user.getId());

        for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
            IdentityType member = membership.getMember();

            if (User.class.isInstance(member)) {
                User userMember = (User) member;

                if (userMember.getId().equals(user.getId())) {
                    getConfig().getMemberships().remove(membership);
                }
            }
        }

        flushUsers();
        flushMemberships();
    }

    @Override
    public Agent getAgent(String id) {
        return getUser(id);
    }

    @Override
    public User getUser(String id) {
        SimpleUser storedUser = getConfig().getUsers().get(id);

        return storedUser;
    }

    @Override
    public Group getGroup(String groupId) {
        SimpleGroup group = getConfig().getGroups().get(groupId);

        return group;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        Group group = getGroup(name);
        Group parentGroup = group.getParentGroup();

        if (parentGroup == null || !parentGroup.getName().equals(parent.getName())) {
            group = null;
        }

        return group;
    }

    @Override
    public Role getRole(String role) {
        SimpleRole fileRole = (SimpleRole) getConfig().getRoles().get(role);

        return fileRole;
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        GroupRole membership = new SimpleGroupRole(member, role, group);

        getConfig().getMemberships().add(membership);

        flushMemberships();

        return membership;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
            boolean match = false;

            if (member != null) {
                Agent memberAgent = (Agent) member;
                Agent agent = (Agent) membership.getMember();

                if (!(agent != null && memberAgent != null && agent.getId().equals(memberAgent.getId()))) {
                    continue;
                }
            }

            if (role != null && group != null) {
                match = membership.getRole() != null && role.getName().equals(membership.getRole().getName())
                        && membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
            } else if (group != null) {
                match = membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
            } else if (role != null) {
                match = membership.getRole() != null && role.getName().equals(membership.getRole().getName());
            }

            if (match) {
                getConfig().getMemberships().remove(membership);
            }
        }

        flushMemberships();
    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
        for (GroupRole membership : new ArrayList<GroupRole>(getConfig().getMemberships())) {
            boolean match = false;

            if (member != null) {
                Agent memberAgent = (Agent) member;
                Agent agent = (Agent) membership.getMember();

                if (!(agent != null && memberAgent != null && agent.getId().equals(memberAgent.getId()))) {
                    continue;
                }
            }

            if (role != null && group != null) {
                match = membership.getRole() != null && role.getName().equals(membership.getRole().getName())
                        && membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
            } else if (group != null) {
                match = membership.getGroup() != null && group.getName().equals(membership.getGroup().getName());
            } else if (role != null) {
                match = membership.getRole() != null && role.getName().equals(membership.getRole().getName());
            }

            if (match) {
                return membership;
            }
        }

        return null;
    }

    private void searchForIdentityTypeAttributes(List<? extends IdentityType> users, IdentityQuery identityQuery) {
        Set<Entry<QueryParameter, Object[]>> entrySet = identityQuery.getParameters().entrySet();

        for (IdentityType fileUser : new ArrayList<IdentityType>(users)) {
            for (Entry<QueryParameter, Object[]> entry : entrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] queryParameterValues = entry.getValue();

                if (IdentityType.AttributeParameter.class.isInstance(queryParameter) && queryParameterValues != null) {
                    IdentityType.AttributeParameter customParameter = (AttributeParameter) queryParameter;
                    Attribute<Serializable> userAttribute = fileUser.getAttribute(customParameter.getName());
                    boolean match = false;

                    if (userAttribute != null && userAttribute.getValue() != null) {
                        int count = queryParameterValues.length;

                        for (Object value : queryParameterValues) {
                            if (userAttribute.getValue().getClass().isArray()) {
                                Object[] userValues = (Object[]) userAttribute.getValue();

                                for (Object object : userValues) {
                                    if (object.equals(value)) {
                                        count--;
                                    }
                                }
                            } else {
                                if (value.equals(userAttribute.getValue())) {
                                    count--;
                                }
                            }
                        }

                        if (count <= 0) {
                            match = true;
                        }
                    }

                    if (!match) {
                        users.remove(fileUser);
                    }
                }
            }
        }
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        Class<T> identityTypeClass = identityQuery.getIdentityType();

        Set entries = null;
        
        if (isUserType(identityTypeClass)) {
            entries = getConfig().getUsers().entrySet();
        } else if (isRoleType(identityTypeClass)) {
            entries = getConfig().getRoles().entrySet();
        } else if (isGroupType(identityTypeClass)) {
            entries = getConfig().getGroups().entrySet();
        }
        
        List<T> users = new ArrayList<T>();
        
        for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
            Entry<String, IdentityType> entry = (Entry<String, IdentityType>) iterator.next();

            IdentityType storedIdentityType = entry.getValue();
            
            if (isUserType(identityTypeClass)) {
                User user = (User) storedIdentityType;
                
                if (identityQuery.getParameters().containsKey(User.ID)) {
                    Object[] values = identityQuery.getParameters().get(User.ID);
                    
                    if (!user.getId().equals(values[0].toString())) {
                        continue;
                    }
                }
                
                if (identityQuery.getParameters().containsKey(User.EMAIL)) {
                    Object[] values = identityQuery.getParameters().get(User.EMAIL);
                    
                    if (user.getEmail() == null || !user.getEmail().equals(values[0].toString())) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(User.FIRST_NAME)) {
                    Object[] values = identityQuery.getParameters().get(User.FIRST_NAME);
                    
                    if (user.getFirstName() == null || !user.getFirstName().equals(values[0].toString())) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(User.LAST_NAME)) {
                    Object[] values = identityQuery.getParameters().get(User.LAST_NAME);
                    
                    if (user.getLastName() == null || !user.getLastName().equals(values[0].toString())) {
                        continue;
                    }
                }
            }
            
            if (isRoleType(identityTypeClass)) {
                Role role = (Role) storedIdentityType;
                
                if (identityQuery.getParameters().containsKey(Role.NAME)) {
                    Object[] values = identityQuery.getParameters().get(Role.NAME);
                    
                    if (!role.getName().equals(values[0].toString())) {
                        continue;
                    }
                }
            }
            
            if (isGroupType(identityTypeClass)) {
                Group group = (Group) storedIdentityType;
                
                if (identityQuery.getParameters().containsKey(Group.NAME)) {
                    Object[] values = identityQuery.getParameters().get(Group.NAME);
                    
                    if (!group.getName().equals(values[0].toString())) {
                        continue;
                    }
                }
                
                if (identityQuery.getParameters().containsKey(Group.PARENT)) {
                    Object[] values = identityQuery.getParameters().get(Group.PARENT);
                    
                    if (group.getParentGroup() == null || !group.getParentGroup().getName().equals(values[0].toString())) {
                        continue;
                    }
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.ENABLED)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.ENABLED);
                String enabled = String.valueOf(storedIdentityType.isEnabled());

                if (!enabled.equals(values[0].toString())) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_DATE)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_DATE);
                long storedDateInMillis = storedIdentityType.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis != providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_BEFORE)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_BEFORE);
                long storedDateInMillis = storedIdentityType.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis > providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_AFTER)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_AFTER);
                long storedDateInMillis = storedIdentityType.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis < providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)
                    || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)
                    || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                if (storedIdentityType.getExpirationDate() != null) {
                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_DATE);
                        long storedDateInMillis = storedIdentityType.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis != providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_BEFORE);
                        long storedDateInMillis = storedIdentityType.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis > providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_AFTER);
                        long storedDateInMillis = storedIdentityType.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis < providedDateInMillis) {
                            continue;
                        }
                    }
                } else {
                    continue;
                }
            }

            users.add((T) storedIdentityType);
        }

        Collection<T> selectedUsers = users;

        if (users.isEmpty()) {
            selectedUsers = (Collection<T>) getConfig().getUsers().values();
        }

        if (identityQuery.getParameters().containsKey(User.HAS_ROLE)
                || identityQuery.getParameters().containsKey(User.MEMBER_OF)
                || identityQuery.getParameters().containsKey(User.HAS_GROUP_ROLE)
                || identityQuery.getParameters().containsKey(User.ROLE_OF)
                || identityQuery.getParameters().containsKey(User.HAS_MEMBER)) {
            List<T> fileteredUsers = new ArrayList<T>();

            List<QueryParameter> toSearch = new ArrayList<QueryParameter>();

            toSearch.add(IdentityType.HAS_ROLE);
            toSearch.add(IdentityType.MEMBER_OF);
            toSearch.add(IdentityType.HAS_GROUP_ROLE);
            toSearch.add(IdentityType.ROLE_OF);
            toSearch.add(IdentityType.HAS_MEMBER);

            for (T fileUser : new ArrayList<T>(selectedUsers)) {
                for (QueryParameter queryParameter : toSearch) {
                    Object[] values = identityQuery.getParameters().get(queryParameter);

                    if (values == null) {
                        continue;
                    }

                    int count = values.length;

                    for (GroupRole membership : getConfig().getMemberships()) {
                        if (isUserType(fileUser.getClass()) && isUserType(membership.getMember().getClass())) {
                            User selectedUser = (User) fileUser;
                            User memberUser = (User) membership.getMember();

                            if (!selectedUser.getId().equals(memberUser.getId())) {
                                continue;
                            }
                        }

                        if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE) && membership.getGroup() != null
                                && membership.getRole() != null) {
                            for (Object groupNames : values) {
                                GroupRole groupRole = (GroupRole) groupNames;

                                if (groupRole.getGroup().getName().equals(membership.getGroup().getName())
                                        && groupRole.getRole().getName().equals(membership.getRole().getName())) {
                                    count--;
                                }
                            }
                        } else if (queryParameter.equals(IdentityType.HAS_ROLE) && membership.getRole() != null) {
                            for (Object roleNames : values) {
                                if (roleNames.equals(membership.getRole().getName())) {
                                    count--;
                                }
                            }
                        } else if (queryParameter.equals(IdentityType.MEMBER_OF) && membership.getGroup() != null) {
                            for (Object groupNames : values) {
                                if (groupNames.equals(membership.getGroup().getName())) {
                                    count--;
                                }
                            }
                        } else if (queryParameter.equals(IdentityType.ROLE_OF) && membership.getRole() != null) {
                            for (Object member : values) {
                                Agent agent = (Agent) member;

                                if (agent != null && agent.getKey().equals(membership.getMember().getKey()) && membership.getRole().getKey().equals(fileUser.getKey())) {
                                    count--;
                                }
                            }
                        } else if (queryParameter.equals(IdentityType.HAS_MEMBER) && membership.getGroup() != null) {
                            for (Object member : values) {
                                Agent agent = (Agent) member;

                                if (agent != null && agent.getKey().equals(membership.getMember().getKey())
                                        && membership.getGroup().getKey().equals(fileUser.getKey())) {
                                    count--;
                                }
                            }
                        }
                    }

                    if (count <= 0) {
                        fileteredUsers.add(fileUser);
                    }
                }
            }

            users.retainAll(fileteredUsers);
        }

        searchForIdentityTypeAttributes(users, identityQuery);

        return users;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw createNotImplementedYetException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        throw createNotImplementedYetException();
    }

    @Override
    public void removeAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

}
