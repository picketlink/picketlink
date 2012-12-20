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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private File usersFile;
    private File rolesFile = new File("/tmp/pl-idm-work/pl-idm-roles.db");
    private File groupsFile = new File("/tmp/pl-idm-work/pl-idm-groups.db");
    private File membershipsFile = new File("/tmp/pl-idm-work/pl-idm-memberships.db");

    private Map<String, FileUser> users = new HashMap<String, FileUser>();
    private Map<String, Role> roles = new HashMap<String, Role>();
    private Map<String, FileGroup> groups = new HashMap<String, FileGroup>();
    private List<FileMembership> memberships = new ArrayList<FileMembership>();

    private FileChangeListener changeListener = new FileChangeListener(this);

    private FileIdentityStoreConfiguration config;
    private IdentityStoreInvocationContext context;

    @Override
    public void setup(FileIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
        initialize();
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
     * Initializes the store.
     * </p>
     */
    private void initialize() {
        initDataFiles();

        loadUsers();
        loadRoles();
        loadGroups();
        loadMemberships();
    }

    /**
     * <p>
     * Initializes the files used to store the informations.
     * </p>
     */
    private void initDataFiles() {
        File workingDirectoryFile = initWorkingDirectory();

        this.usersFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-users.db"));
        this.rolesFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-roles.db"));
        this.groupsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-groups.db"));
        this.membershipsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-memberships.db"));
    }

    /**
     * <p>
     * Initializes the working directory.
     * </p>
     * 
     * @return
     */
    private File initWorkingDirectory() {
        String workingDir = getConfig().getWorkingDir();

        File workingDirectoryFile = new File(workingDir);

        if (!workingDirectoryFile.exists()) {
            workingDirectoryFile.mkdirs();
        }

        return workingDirectoryFile;
    }

    /**
     * <p>
     * Check if the specified {@link File} exists. If not create it.
     * </p>
     * 
     * @param file
     * @return
     */
    private File checkAndCreateFile(File file) {
        if (getConfig().isAlwaysCreateFiles() && file.exists()) {
            file.delete();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }

        return file;
    }

    /**
     * <p>
     * Load all persisted groups from the filesystem.
     * </p>
     */
    private void loadGroups() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(groupsFile);
            ois = new ObjectInputStream(fis);

            this.groups = (Map<String, FileGroup>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted memberships from the filesystem.
     * </p>
     */
    private void loadMemberships() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(membershipsFile);
            ois = new ObjectInputStream(fis);

            this.memberships = (List<FileMembership>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted roles from the filesystem.
     * </p>
     */
    private void loadRoles() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(rolesFile);
            ois = new ObjectInputStream(fis);

            this.roles = (Map<String, Role>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted users from the filesystem.
     * </p>
     */
    private void loadUsers() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(usersFile);
            ois = new ObjectInputStream(fis);

            this.users = (Map<String, FileUser>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Flush all changes made to users to the filesystem.
     * </p>
     */
    synchronized void flushUsers() {
        try {
            FileOutputStream fos = new FileOutputStream(this.usersFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.users);
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
            FileOutputStream fos = new FileOutputStream(this.rolesFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.roles);
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
            FileOutputStream fos = new FileOutputStream(this.groupsFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.groups);
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
            FileOutputStream fos = new FileOutputStream(this.membershipsFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.memberships);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (isUserType(identityTypeClass)) {
            User user = (User) identityType;

            FileUser fileUser;

            if (!(user instanceof FileUser)) {
                fileUser = new FileUser(user.getId());

                fileUser.setFirstName(user.getFirstName());
                fileUser.setLastName(user.getLastName());
                fileUser.setEmail(user.getEmail());
                fileUser.setEnabled(user.isEnabled());
                fileUser.setCreatedDate(user.getCreatedDate());
                fileUser.setExpirationDate(user.getExpirationDate());

                for (Attribute<? extends Serializable> attrib : user.getAttributes()) {
                    fileUser.setAttribute(attrib);
                }
            } else {
                fileUser = (FileUser) user;
            }

            this.users.put(user.getId(), fileUser);

            flushUsers();
        } else if (isGroupType(identityTypeClass)) {
            Group group = (Group) identityType;
            FileGroup fileGroup = new FileGroup();

            fileGroup.setName(group.getName());
            fileGroup.setEnabled(group.isEnabled());
            fileGroup.setCreatedDate(group.getCreatedDate());
            fileGroup.setExpirationDate(group.getExpirationDate());

            if (group.getParentGroup() != null) {
                fileGroup.setParentGroup(getGroup(group.getParentGroup().getName()));
            }

            for (Attribute<? extends Serializable> attrib : group.getAttributes()) {
                fileGroup.setAttribute(attrib);
            }

            this.groups.put(group.getName(), fileGroup);

            flushGroups();
        } else if (isRoleType(identityTypeClass)) {
            Role role = (Role) identityType;
            FileRole fileRole = new FileRole();

            fileRole.setName(role.getName());
            fileRole.setEnabled(role.isEnabled());
            fileRole.setCreatedDate(role.getCreatedDate());
            fileRole.setExpirationDate(role.getExpirationDate());

            for (Attribute<? extends Serializable> attrib : role.getAttributes()) {
                fileRole.setAttribute(attrib);
            }

            this.roles.put(role.getName(), fileRole);

            flushRoles();
        }
    }

    @Override
    public void update(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (isUserType(identityTypeClass)) {
            User user = (User) identityType;
            FileUser fileUser = null;

            if (!FileUser.class.isInstance(user)) {
                fileUser = (FileUser) getUser(user.getId());

                fileUser.setFirstName(user.getFirstName());
                fileUser.setLastName(user.getLastName());
                fileUser.setEmail(user.getEmail());
                fileUser.setEnabled(user.isEnabled());
                fileUser.setCreatedDate(user.getCreatedDate());
                fileUser.setExpirationDate(user.getExpirationDate());

                for (Attribute<? extends Serializable> attribute : fileUser.getAttributes()) {
                    fileUser.removeAttribute(attribute.getName());
                }

                for (Attribute<? extends Serializable> attrib : user.getAttributes()) {
                    fileUser.setAttribute(attrib);
                }
            } else {
                fileUser = (FileUser) user;
            }

            this.users.put(user.getId(), fileUser);

            flushUsers();
        } else if (isGroupType(identityTypeClass)) {
            Group group = (Group) identityType;
            FileGroup fileGroup = null;

            if (!FileGroup.class.isInstance(group)) {
                fileGroup = (FileGroup) getGroup(group.getName());

                fileGroup.setEnabled(group.isEnabled());
                fileGroup.setCreatedDate(group.getCreatedDate());
                fileGroup.setExpirationDate(group.getExpirationDate());

                for (Attribute<? extends Serializable> attribute : fileGroup.getAttributes()) {
                    fileGroup.removeAttribute(attribute.getName());
                }

                for (Attribute<? extends Serializable> attrib : group.getAttributes()) {
                    fileGroup.setAttribute(attrib);
                }
            } else {
                fileGroup = (FileGroup) group;
            }

            this.groups.put(group.getName(), fileGroup);

            flushGroups();
        } else if (isRoleType(identityTypeClass)) {
            Role role = (Role) identityType;
            FileRole fileRole = null;

            if (!FileRole.class.isInstance(role)) {
                fileRole = (FileRole) getRole(role.getName());

                fileRole.setEnabled(role.isEnabled());
                fileRole.setCreatedDate(role.getCreatedDate());
                fileRole.setExpirationDate(role.getExpirationDate());

                for (Attribute<? extends Serializable> attribute : fileRole.getAttributes()) {
                    fileRole.removeAttribute(attribute.getName());
                }

                for (Attribute<? extends Serializable> attrib : role.getAttributes()) {
                    fileRole.setAttribute(attrib);
                }
            } else {
                fileRole = (FileRole) role;
            }

            this.roles.put(role.getName(), fileRole);

            flushRoles();
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;

            this.users.remove(user.getId());

            for (FileMembership membership : new ArrayList<FileMembership>(this.memberships)) {
                IdentityType member = membership.getMember();

                if (User.class.isInstance(member)) {
                    User userMember = (User) member;

                    if (userMember.getId().equals(user.getId())) {
                        this.memberships.remove(membership);
                    }
                }
            }

            flushUsers();
            flushMemberships();
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            this.groups.remove(group.getName());

            for (FileMembership membership : new ArrayList<FileMembership>(this.memberships)) {
                IdentityType member = membership.getMember();

                if (Group.class.isInstance(member)) {
                    Group groupMember = (Group) member;
                    Group groupMembership = membership.getGroup();

                    if (groupMember.getName().equals(group.getName())
                            || (groupMembership != null && groupMembership.getName().equals(group.getName()))) {
                        this.memberships.remove(membership);
                    }
                }
            }

            flushGroups();
            flushMemberships();
        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            this.roles.remove(role.getName());

            for (FileMembership membership : new ArrayList<FileMembership>(this.memberships)) {
                IdentityType member = membership.getMember();

                if (Group.class.isInstance(member)) {
                    Role roleMember = (Role) member;
                    Role roleMembership = membership.getRole();

                    if (roleMember.getName().equals(role.getName())
                            || (roleMembership != null && roleMembership.getName().equals(role.getName()))) {
                        this.memberships.remove(membership);
                    }
                }
            }

            flushRoles();
            flushMemberships();
        }
    }

    @Override
    public Agent getAgent(String id) {
        return getUser(id);
    }

    @Override
    public User getUser(String id) {
        FileUser storedUser = this.users.get(id);

        return storedUser;
    }

    @Override
    public Group getGroup(String groupId) {
        FileGroup group = this.groups.get(groupId);

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
        FileRole fileRole = (FileRole) this.roles.get(role);

        return fileRole;
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        FileMembership membership = new FileMembership(member, group, role);

        this.memberships.add(membership);

        flushMemberships();

        return membership;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        for (GroupRole membership : new ArrayList<FileMembership>(this.memberships)) {
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
                this.memberships.remove(membership);
            }
        }

        flushMemberships();
    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
        for (GroupRole membership : new ArrayList<FileMembership>(this.memberships)) {
            boolean match = false;

            if (role != null) {
                match = membership.getRole() != null && role.equals(membership.getRole());
            } else {
                match = true;
            }

            if (member != null) {
                match = membership.getMember() != null && member.equals(membership.getMember());
            } else {
                match = true;
            }

            if (group != null) {
                match = membership.getGroup() != null && group.equals(membership.getGroup());
            } else {
                match = true;
            }

            if (match) {
                return membership;
            }
        }

        return null;
    }

    /*
     * @Override public List<User> executeQuery(IdentityStoreInvocationContext ctx, UserQuery query, Range range) { List<User>
     * users = new ArrayList<User>();
     * 
     * for (Entry<String, FileUser> entry : this.users.entrySet()) { FileUser fileUser = entry.getValue();
     * 
     * if (query.getName() != null) { if (!fileUser.getId().equals(query.getName())) { continue; } }
     * 
     * if (query.getEnabled() != fileUser.isEnabled()) { continue; }
     * 
     * if (query.getEmail() != null) { if (!query.getEmail().equals(fileUser.getEmail())) { continue; } }
     * 
     * if (query.getFirstName() != null) { if (!query.getFirstName().equals(fileUser.getFirstName())) { continue; } }
     * 
     * if (query.getLastName() != null) { if (!query.getLastName().equals(fileUser.getLastName())) { continue; } }
     * 
     * users.add(fileUser); }
     * 
     * Collection<? extends User> selectedUsers = users;
     * 
     * if (users.isEmpty()) { selectedUsers = this.users.values(); }
     * 
     * if (query.getRole() != null || query.getRelatedGroup() != null) { List<User> fileteredUsers = new ArrayList<User>();
     * 
     * for (User fileUser : new ArrayList<User>(selectedUsers)) { for (Membership membership : this.memberships) { if
     * ((query.getRole() != null && membership.getRole() == null) || (query.getRelatedGroup() != null && membership.getGroup()
     * == null) || membership.getMember() == null) { continue; }
     * 
     * if (!membership.getMember().equals(fileUser)) { continue; }
     * 
     * if (query.getRole() != null) { if (!membership.getRole().equals(query.getRole())) { continue; } }
     * 
     * if (query.getRelatedGroup() != null) { if (!membership.getGroup().equals(query.getRelatedGroup())) { continue; } }
     * 
     * fileteredUsers.add(fileUser); } }
     * 
     * users.retainAll(fileteredUsers); }
     * 
     * Map<String, String[]> queryAttributes = query.getAttributeFilters();
     * 
     * searchForIdentityTypeAttributes(users, queryAttributes);
     * 
     * return users; }
     * 
     * @Override public List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range) {
     * List<Group> groups = new ArrayList<Group>();
     * 
     * for (Entry<String, FileGroup> entry : this.groups.entrySet()) { FileGroup fileGroup = entry.getValue();
     * 
     * if (query.getName() != null) { if (!fileGroup.getKey().equals(query.getName())) { continue; } }
     * 
     * if (query.getId() != null) { if (!query.getId().equals(fileGroup.getId())) { continue; } }
     * 
     * if (query.getParentGroup() != null) { if (fileGroup.getParentGroup() == null ||
     * !query.getParentGroup().equals(fileGroup.getParentGroup())) { continue; } }
     * 
     * groups.add(fileGroup); }
     * 
     * Collection<? extends Group> selectedGroups = groups;
     * 
     * if (groups.isEmpty()) { selectedGroups = this.groups.values(); }
     * 
     * if (query.getRole() != null || query.getRelatedUser() != null) { List<Group> fileteredGroups = new ArrayList<Group>();
     * 
     * for (Group fileGroup : new ArrayList<Group>(selectedGroups)) { for (Membership membership : this.memberships) { if
     * ((query.getRole() != null && membership.getRole() == null) || (query.getRelatedUser() != null && membership.getMember()
     * == null) || membership.getGroup() == null) { continue; }
     * 
     * if (!membership.getGroup().equals(fileGroup)) { continue; }
     * 
     * if (query.getRole() != null) { if (!membership.getRole().equals(query.getRole())) { continue; } }
     * 
     * if (query.getRelatedUser() != null) { if (!membership.getMember().equals(query.getRelatedUser())) { continue; } }
     * 
     * fileteredGroups.add(fileGroup); } }
     * 
     * groups.retainAll(fileteredGroups); }
     * 
     * if (query.getAttributeFilters() != null && !query.getAttributeFilters().isEmpty()) {
     * searchForIdentityTypeAttributes(groups, query.getAttributeFilters()); }
     * 
     * return groups; }
     * 
     * @Override public List<Role> executeQuery(IdentityStoreInvocationContext ctx, RoleQuery query, Range range) { List<Role>
     * roles = new ArrayList<Role>();
     * 
     * if (query.getName() != null) { Role role = getRole(ctx, query.getName());
     * 
     * if (role != null) { roles.add(role); } }
     * 
     * if (query.getOwner() != null || query.getGroup() != null) { for (Membership membership : this.memberships) { if
     * (membership.getRole() == null) { continue; }
     * 
     * if (query.getOwner() != null) { if (!(membership.getMember() != null &&
     * membership.getMember().getKey().equals(query.getOwner().getKey()))) { continue; } }
     * 
     * if (query.getGroup() != null) { if (!(membership.getGroup() != null &&
     * membership.getGroup().getKey().equals(query.getGroup().getKey()))) { continue; } }
     * 
     * roles.add(membership.getRole()); } }
     * 
     * if (query.getAttributeFilters() != null && !query.getAttributeFilters().isEmpty()) {
     * searchForIdentityTypeAttributes(roles, query.getAttributeFilters()); }
     * 
     * return roles; }
     * 
     * @Override public List<Membership> executeQuery(IdentityStoreInvocationContext ctx, MembershipQuery query, Range range) {
     * List<Membership> memberships = new ArrayList<Membership>();
     * 
     * for (Membership membership : this.memberships) { if ((query.getRole() != null && membership.getRole() == null) ||
     * (query.getGroup() != null && membership.getGroup() == null) || (query.getUser() != null && membership.getMember() ==
     * null)) { continue; }
     * 
     * if (query.getRole() != null) { if (!membership.getRole().equals(query.getRole())) { continue; } }
     * 
     * if (query.getGroup() != null) { if (!membership.getGroup().equals(query.getGroup())) { continue; } }
     * 
     * if (query.getUser() != null) { if (!membership.getMember().equals(query.getUser())) { continue; } }
     * 
     * memberships.add(membership); }
     * 
     * return memberships; }
     */

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        if (identityType instanceof FileUser) {
            FileUser user = (FileUser) identityType;
            FileUser fileUser = (FileUser) getUser(user.getId());

            fileUser.setAttribute(attribute);

            flushUsers();
        } else if (identityType instanceof FileRole) {
            FileRole role = (FileRole) identityType;
            FileRole fileRole = (FileRole) getRole(role.getName());

            fileRole.setAttribute(attribute);

            flushRoles();
        } else if (identityType instanceof FileGroup) {
            FileGroup group = (FileGroup) identityType;
            FileGroup fileGroup = (FileGroup) getGroup(group.getName());

            fileGroup.setAttribute(attribute);

            flushRoles();
        } else {
            throw throwsNotSupportedIdentityType(identityType);
        }
    }

    @Override
    public void removeAttribute(IdentityType identityType, String name) {
        if (identityType instanceof FileUser) {
            FileUser user = (FileUser) identityType;
            FileUser fileUser = (FileUser) getUser(user.getId());

            if (fileUser != null) {
                this.users.remove(fileUser.getId());
            }

            flushUsers();
        } else if (identityType instanceof FileRole) {
            FileRole role = (FileRole) identityType;
            FileRole fileRole = (FileRole) getRole(role.getName());

            if (fileRole != null) {
                this.roles.remove(fileRole.getName());
            }

            flushRoles();
        } else if (identityType instanceof FileGroup) {
            FileGroup group = (FileGroup) identityType;
            FileGroup fileGroup = (FileGroup) getGroup(group.getName());

            if (fileGroup != null) {
                this.groups.remove(fileGroup.getName());
            }

            flushRoles();
        } else {
            throwsNotSupportedIdentityType(identityType);
        }
    }

    // TODO method no longer necessary?
    /*
     * @Override public String[] getAttributeValues(IdentityStoreInvocationContext ctx, IdentityType identityType, String name)
     * { if (identityType instanceof FileUser) { FileUser user = (FileUser) identityType; FileUser fileUser = (FileUser)
     * getUser(ctx, user.getId());
     * 
     * if (fileUser != null) { return fileUser.getAttributeValues(name); } } else if (identityType instanceof FileRole) {
     * FileRole role = (FileRole) identityType; FileRole fileRole = (FileRole) getRole(ctx, role.getName());
     * 
     * if (fileRole != null) { return fileRole.getAttributeValues(name); } } else if (identityType instanceof FileGroup) {
     * FileGroup group = (FileGroup) identityType; FileGroup fileGroup = (FileGroup) getGroup(ctx, group.getName());
     * 
     * if (fileGroup != null) { return fileGroup.getAttributeValues(name); }
     * 
     * flushRoles(); } else { throwsNotSupportedIdentityType(identityType); }
     * 
     * return null; }
     */

    // TODO don't need this method now?
    /*
     * @Override public Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, IdentityType identityType) { if
     * (identityType instanceof FileUser) { FileUser user = (FileUser) identityType; FileUser fileUser = (FileUser) getUser(ctx,
     * user.getId());
     * 
     * if (fileUser != null) { return fileUser.getAttributes(); } } else if (identityType instanceof FileRole) { FileRole role =
     * (FileRole) identityType; FileRole fileRole = (FileRole) getRole(ctx, role.getName());
     * 
     * if (fileRole != null) { return fileRole.getAttributes(); } } else if (identityType instanceof FileGroup) { FileGroup
     * group = (FileGroup) identityType; FileGroup fileGroup = (FileGroup) getGroup(ctx, group.getName());
     * 
     * if (fileGroup != null) { return fileGroup.getAttributes(); }
     * 
     * flushRoles(); } else { throwsNotSupportedIdentityType(identityType); }
     * 
     * return null; }
     */

    // @Override
    // public boolean validateCredential(User user, Object credential) {
    // if (credential instanceof PlainTextPassword) {
    // PlainTextPassword password = (PlainTextPassword) credential;
    //
    // User storedUser = getUser(user.getId());
    // String storedPassword = storedUser.<String>getAttribute(USER_PASSWORD_ATTRIBUTE).getValue();
    //
    // return storedPassword != null && storedPassword.equals(password.getValue());
    // } else if (credential instanceof Digest) {
    // Digest digestCredential = (Digest) credential;
    //
    // User storedUser = getUser(user.getId());
    // String storedPassword = storedUser.<String>getAttribute(USER_PASSWORD_ATTRIBUTE).getValue();
    //
    // return DigestUtil.matchCredential(digestCredential, storedPassword.toCharArray());
    // } else if (credential instanceof X509CertificateCredentials) {
    // X509CertificateCredentials certCredential = (X509CertificateCredentials) credential;
    //
    // User storedUser = getUser(user.getId());
    //
    // String storedCert = storedUser.<String>getAttribute(USER_CERTIFICATE_ATTRIBUTE).getValue();
    //
    // if (storedCert != null) {
    // try {
    // return storedCert.equals(new String(Base64.encodeBytes(certCredential.getCertificate().getEncoded())));
    // } catch (CertificateEncodingException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // } else {
    // throwsNotSupportedCredentialType(credential);
    // }
    //
    // return false;
    // }

    // @Override
    // public void updateCredential(User user, Object credential) {
    // if (credential instanceof PlainTextPassword) {
    // PlainTextPassword password = (PlainTextPassword) credential;
    //
    // User storedUser = getUser(user.getId());
    //
    // storedUser.setAttribute(new Attribute<String>(USER_PASSWORD_ATTRIBUTE, new String(password.getValue())));
    //
    // flushUsers();
    // } else if (credential instanceof X509CertificateCredentials) {
    // X509CertificateCredentials certCredential = (X509CertificateCredentials) credential;
    //
    // User storedUser = getUser(user.getId());
    //
    // try {
    // storedUser.setAttribute(new Attribute<String>(USER_CERTIFICATE_ATTRIBUTE,
    // new String(Base64.encodeBytes(certCredential.getCertificate().getEncoded()))));
    // } catch (CertificateEncodingException e) {
    // throw new RuntimeException(e);
    // }
    // } else {
    // throwsNotSupportedCredentialType(credential);
    // }
    // }

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
        List<T> users = new ArrayList<T>();

        for (Entry<String, FileUser> entry : this.users.entrySet()) {
            FileUser fileUser = entry.getValue();

            if (identityQuery.getParameters().containsKey(User.ID)) {
                Object[] values = identityQuery.getParameters().get(User.ID);

                if (!fileUser.getId().equals(values[0].toString())) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.ENABLED)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.ENABLED);
                String enabled = String.valueOf(fileUser.isEnabled());

                if (!enabled.equals(values[0].toString())) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_DATE)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_DATE);
                long storedDateInMillis = fileUser.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis != providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_BEFORE)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_BEFORE);
                long storedDateInMillis = fileUser.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis > providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.CREATED_AFTER)) {
                Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_AFTER);
                long storedDateInMillis = fileUser.getCreatedDate().getTime();
                long providedDateInMillis = ((Date) values[0]).getTime();

                if (storedDateInMillis < providedDateInMillis) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)
                    || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE) 
                    || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                if (fileUser.getExpirationDate() != null) {
                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_DATE);
                        long storedDateInMillis = fileUser.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis != providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_BEFORE);
                        long storedDateInMillis = fileUser.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis > providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_AFTER);
                        long storedDateInMillis = fileUser.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis < providedDateInMillis) {
                            continue;
                        }
                    }
                } else {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(User.EMAIL)) {
                Object[] values = identityQuery.getParameters().get(User.EMAIL);

                if (fileUser.getEmail() == null || !fileUser.getEmail().equals(values[0].toString())) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(User.FIRST_NAME)) {
                Object[] values = identityQuery.getParameters().get(User.FIRST_NAME);

                if (fileUser.getFirstName() == null || !fileUser.getFirstName().equals(values[0].toString())) {
                    continue;
                }
            }

            if (identityQuery.getParameters().containsKey(User.LAST_NAME)) {
                Object[] values = identityQuery.getParameters().get(User.LAST_NAME);

                if (fileUser.getLastName() == null || !fileUser.getLastName().equals(values[0].toString())) {
                    continue;
                }
            }

            users.add((T) fileUser);
        }

        Collection<T> selectedUsers = users;

        if (users.isEmpty()) {
            selectedUsers = (Collection<T>) this.users.values();
        }

        if (identityQuery.getParameters().containsKey(User.HAS_ROLE)
                || identityQuery.getParameters().containsKey(User.MEMBER_OF)
                || identityQuery.getParameters().containsKey(User.HAS_GROUP_ROLE)) {
            List<T> fileteredUsers = new ArrayList<T>();

            List<QueryParameter> toSearch = new ArrayList<QueryParameter>();

            toSearch.add(IdentityType.HAS_ROLE);
            toSearch.add(IdentityType.MEMBER_OF);
            toSearch.add(IdentityType.HAS_GROUP_ROLE);

            for (T fileUser : new ArrayList<T>(selectedUsers)) {
                for (QueryParameter queryParameter : toSearch) {
                    Object[] values = identityQuery.getParameters().get(queryParameter);

                    if (values == null) {
                        continue;
                    }

                    int count = values.length;

                    for (FileMembership membership : this.memberships) {
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
                        }
                    }

                    if (count == 0) {
                        fileteredUsers.add(fileUser);
                    }
                }
            }

            users.retainAll(fileteredUsers);
        }

        searchForIdentityTypeAttributes(users, identityQuery);

        return users;
    }

    // @Override
    // public List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range) {
    // List<Group> groups = new ArrayList<Group>();
    //
    // for (Entry<String, FileGroup> entry : this.groups.entrySet()) {
    // FileGroup fileGroup = entry.getValue();
    //
    // if (query.getName() != null) {
    // if (!fileGroup.getKey().equals(query.getName())) {
    // continue;
    // }
    // }
    //
    // if (query.getId() != null) {
    // if (!query.getId().equals(fileGroup.getId())) {
    // continue;
    // }
    // }
    //
    // if (query.getParentGroup() != null) {
    // if (fileGroup.getParentGroup() == null || !query.getParentGroup().equals(fileGroup.getParentGroup())) {
    // continue;
    // }
    // }
    //
    // groups.add(fileGroup);
    // }
    //
    // Collection<? extends Group> selectedGroups = groups;
    //
    // if (groups.isEmpty()) {
    // selectedGroups = this.groups.values();
    // }
    //
    // if (query.getRole() != null || query.getRelatedUser() != null) {
    // List<Group> fileteredGroups = new ArrayList<Group>();
    //
    // for (Group fileGroup : new ArrayList<Group>(selectedGroups)) {
    // for (Membership membership : this.memberships) {
    // if ((query.getRole() != null && membership.getRole() == null)
    // || (query.getRelatedUser() != null && membership.getMember() == null)
    // || membership.getGroup() == null) {
    // continue;
    // }
    //
    // if (!membership.getGroup().equals(fileGroup)) {
    // continue;
    // }
    //
    // if (query.getRole() != null) {
    // if (!membership.getRole().equals(query.getRole())) {
    // continue;
    // }
    // }
    //
    // if (query.getRelatedUser() != null) {
    // if (!membership.getMember().equals(query.getRelatedUser())) {
    // continue;
    // }
    // }
    //
    // fileteredGroups.add(fileGroup);
    // }
    // }
    //
    // groups.retainAll(fileteredGroups);
    // }
    //
    // if (query.getAttributeFilters() != null && !query.getAttributeFilters().isEmpty()) {
    // searchForIdentityTypeAttributes(groups, query.getAttributeFilters());
    // }
    //
    // return groups;
    // }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw createNotImplementedYetException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

}
