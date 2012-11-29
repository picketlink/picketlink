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
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.credential.DigestCredential;
import org.picketlink.idm.credential.DigestCredentialUtil;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
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
public class FileBasedIdentityStore implements IdentityStore<IdentityStoreConfiguration> {
    
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
    private String workingDir;
    private boolean alwaysCreateFiles = true;

    public FileBasedIdentityStore() {
        initialize();
    }

    public FileBasedIdentityStore(String workingDir, boolean alwaysCreateFiles) {
        this.workingDir = workingDir;
        this.alwaysCreateFiles = alwaysCreateFiles;
        initialize();
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
        String workingDir = getWorkingDir();

        if (workingDir == null) {
            workingDir = System.getProperty("java.io.tmpdir");
        }

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
        if (this.alwaysCreateFiles && file.exists()) {
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
    public void createUser(User user) {
        FileUser fileUser;

        if (!(user instanceof FileUser)) {
            fileUser = new FileUser(user.getId());
            
            fileUser.setFirstName(user.getFirstName());
            fileUser.setLastName(user.getLastName());
            fileUser.setEmail(user.getEmail());
            
            for (Attribute<? extends Serializable> attrib : user.getAttributes()) {
                fileUser.setAttribute(attrib);
            }
        } else {
            fileUser = (FileUser) user;
        }

        fileUser.setChangeListener(this.changeListener);

        this.users.put(user.getId(), fileUser);

        flushUsers();
    }

    @Override
    public void removeUser(User user) {
        this.users.remove(user.getId());

        flushUsers();
    }

    @Override
    public User getUser(String name) {
        FileUser user = this.users.get(name);

        if (user != null) {
            user.setChangeListener(this.changeListener);
        }

        return user;
    }

    @Override
    public void createGroup(Group group) {
        // FIXME
        //this.groups.put(group.getName(), group);

        // FIXME
        //group.setChangeListener(this.changeListener);

        flushGroups();
    }

    @Override
    public void removeGroup(Group group) {
        this.groups.remove(group.getName());
        flushGroups();
    }

    @Override
    public Group getGroup(String groupId) {
        FileGroup group = this.groups.get(groupId);

        if (group != null) {
            group.setChangeListener(this.changeListener);
        }

        return group;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        // TODO implement this
        return null;
    }

    @Override
    public void createRole(Role role) {
        this.roles.put(role.getName(), role);

        // FIXME need to fix this?
        //role.setChangeListener(this.changeListener);

        flushRoles();
    }

    @Override
    public void removeRole(Role role) {
        this.roles.remove(role.getName());
        flushRoles();
    }

    @Override
    public Role getRole(String role) {
        FileRole fileRole = (FileRole) this.roles.get(role);

        if (fileRole != null) {
            fileRole.setChangeListener(this.changeListener);
        }

        return fileRole;
    }

    @Override
    public Membership createMembership(IdentityType member, Group group, Role role) {
        FileMembership membership = new FileMembership(member, group, role);

        this.memberships.add(membership);

        flushMemberships();

        return membership;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        for (Membership membership : new ArrayList<FileMembership>(this.memberships)) {
            boolean match = false;

            if (role != null) {
                match = membership.getRole() != null && role.equals(membership.getRole());
            }

            if (member != null) {
                match = membership.getMember() != null && member.equals(membership.getMember());
            }

            if (group != null) {
                match = membership.getGroup() != null && group.equals(membership.getGroup());
            }

            if (match) {
                this.memberships.remove(membership);
            }
        }

        flushMemberships();
    }

    @Override
    public Membership getMembership(IdentityType member, Group group, Role role) {
        for (Membership membership : new ArrayList<FileMembership>(this.memberships)) {
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
    @Override
    public List<User> executeQuery(IdentityStoreInvocationContext ctx, UserQuery query, Range range) {
        List<User> users = new ArrayList<User>();

        for (Entry<String, FileUser> entry : this.users.entrySet()) {
            FileUser fileUser = entry.getValue();

            if (query.getName() != null) {
                if (!fileUser.getId().equals(query.getName())) {
                    continue;
                }
            }

            if (query.getEnabled() != fileUser.isEnabled()) {
                continue;
            }

            if (query.getEmail() != null) {
                if (!query.getEmail().equals(fileUser.getEmail())) {
                    continue;
                }
            }

            if (query.getFirstName() != null) {
                if (!query.getFirstName().equals(fileUser.getFirstName())) {
                    continue;
                }
            }

            if (query.getLastName() != null) {
                if (!query.getLastName().equals(fileUser.getLastName())) {
                    continue;
                }
            }

            users.add(fileUser);
        }

        Collection<? extends User> selectedUsers = users;

        if (users.isEmpty()) {
            selectedUsers = this.users.values();
        }

        if (query.getRole() != null || query.getRelatedGroup() != null) {
            List<User> fileteredUsers = new ArrayList<User>();

            for (User fileUser : new ArrayList<User>(selectedUsers)) {
                for (Membership membership : this.memberships) {
                    if ((query.getRole() != null && membership.getRole() == null)
                            || (query.getRelatedGroup() != null && membership.getGroup() == null)
                            || membership.getMember() == null) {
                        continue;
                    }

                    if (!membership.getMember().equals(fileUser)) {
                        continue;
                    }

                    if (query.getRole() != null) {
                        if (!membership.getRole().equals(query.getRole())) {
                            continue;
                        }
                    }

                    if (query.getRelatedGroup() != null) {
                        if (!membership.getGroup().equals(query.getRelatedGroup())) {
                            continue;
                        }
                    }

                    fileteredUsers.add(fileUser);
                }
            }

            users.retainAll(fileteredUsers);
        }

        Map<String, String[]> queryAttributes = query.getAttributeFilters();

        searchForIdentityTypeAttributes(users, queryAttributes);

        return users;
    }

    @Override
    public List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range) {
        List<Group> groups = new ArrayList<Group>();

        for (Entry<String, FileGroup> entry : this.groups.entrySet()) {
            FileGroup fileGroup = entry.getValue();

            if (query.getName() != null) {
                if (!fileGroup.getKey().equals(query.getName())) {
                    continue;
                }
            }

            if (query.getId() != null) {
                if (!query.getId().equals(fileGroup.getId())) {
                    continue;
                }
            }

            if (query.getParentGroup() != null) {
                if (fileGroup.getParentGroup() == null || !query.getParentGroup().equals(fileGroup.getParentGroup())) {
                    continue;
                }
            }

            groups.add(fileGroup);
        }

        Collection<? extends Group> selectedGroups = groups;

        if (groups.isEmpty()) {
            selectedGroups = this.groups.values();
        }

        if (query.getRole() != null || query.getRelatedUser() != null) {
            List<Group> fileteredGroups = new ArrayList<Group>();

            for (Group fileGroup : new ArrayList<Group>(selectedGroups)) {
                for (Membership membership : this.memberships) {
                    if ((query.getRole() != null && membership.getRole() == null)
                            || (query.getRelatedUser() != null && membership.getMember() == null)
                            || membership.getGroup() == null) {
                        continue;
                    }

                    if (!membership.getGroup().equals(fileGroup)) {
                        continue;
                    }

                    if (query.getRole() != null) {
                        if (!membership.getRole().equals(query.getRole())) {
                            continue;
                        }
                    }

                    if (query.getRelatedUser() != null) {
                        if (!membership.getMember().equals(query.getRelatedUser())) {
                            continue;
                        }
                    }

                    fileteredGroups.add(fileGroup);
                }
            }

            groups.retainAll(fileteredGroups);
        }

        if (query.getAttributeFilters() != null && !query.getAttributeFilters().isEmpty()) {
            searchForIdentityTypeAttributes(groups, query.getAttributeFilters());
        }

        return groups;
    }

    @Override
    public List<Role> executeQuery(IdentityStoreInvocationContext ctx, RoleQuery query, Range range) {
        List<Role> roles = new ArrayList<Role>();

        if (query.getName() != null) {
            Role role = getRole(ctx, query.getName());

            if (role != null) {
                roles.add(role);
            }
        }

        if (query.getOwner() != null || query.getGroup() != null) {
            for (Membership membership : this.memberships) {
                if (membership.getRole() == null) {
                    continue;
                }

                if (query.getOwner() != null) {
                    if (!(membership.getMember() != null && membership.getMember().getKey().equals(query.getOwner().getKey()))) {
                        continue;
                    }
                }

                if (query.getGroup() != null) {
                    if (!(membership.getGroup() != null && membership.getGroup().getKey().equals(query.getGroup().getKey()))) {
                        continue;
                    }
                }

                roles.add(membership.getRole());
            }
        }

        if (query.getAttributeFilters() != null && !query.getAttributeFilters().isEmpty()) {
            searchForIdentityTypeAttributes(roles, query.getAttributeFilters());
        }

        return roles;
    }

    @Override
    public List<Membership> executeQuery(IdentityStoreInvocationContext ctx, MembershipQuery query, Range range) {
        List<Membership> memberships = new ArrayList<Membership>();

        for (Membership membership : this.memberships) {
            if ((query.getRole() != null && membership.getRole() == null)
                    || (query.getGroup() != null && membership.getGroup() == null)
                    || (query.getUser() != null && membership.getMember() == null)) {
                continue;
            }

            if (query.getRole() != null) {
                if (!membership.getRole().equals(query.getRole())) {
                    continue;
                }
            }

            if (query.getGroup() != null) {
                if (!membership.getGroup().equals(query.getGroup())) {
                    continue;
                }
            }

            if (query.getUser() != null) {
                if (!membership.getMember().equals(query.getUser())) {
                    continue;
                }
            }

            memberships.add(membership);
        }

        return memberships;
    }*/

    @Override
    public void setAttribute(IdentityType identityType, 
            Attribute<? extends Serializable> attribute) {
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
            throwsNotSupportedIdentityType(identityType);
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
    @Override
    public String[] getAttributeValues(IdentityStoreInvocationContext ctx, IdentityType identityType, String name) {
        if (identityType instanceof FileUser) {
            FileUser user = (FileUser) identityType;
            FileUser fileUser = (FileUser) getUser(ctx, user.getId());

            if (fileUser != null) {
                return fileUser.getAttributeValues(name);
            }
        } else if (identityType instanceof FileRole) {
            FileRole role = (FileRole) identityType;
            FileRole fileRole = (FileRole) getRole(ctx, role.getName());

            if (fileRole != null) {
                return fileRole.getAttributeValues(name);
            }
        } else if (identityType instanceof FileGroup) {
            FileGroup group = (FileGroup) identityType;
            FileGroup fileGroup = (FileGroup) getGroup(ctx, group.getName());

            if (fileGroup != null) {
                return fileGroup.getAttributeValues(name);
            }

            flushRoles();
        } else {
            throwsNotSupportedIdentityType(identityType);
        }

        return null;
    }*/

    // TODO don't need this method now?
    /*@Override
    public Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, IdentityType identityType) {
        if (identityType instanceof FileUser) {
            FileUser user = (FileUser) identityType;
            FileUser fileUser = (FileUser) getUser(ctx, user.getId());

            if (fileUser != null) {
                return fileUser.getAttributes();
            }
        } else if (identityType instanceof FileRole) {
            FileRole role = (FileRole) identityType;
            FileRole fileRole = (FileRole) getRole(ctx, role.getName());

            if (fileRole != null) {
                return fileRole.getAttributes();
            }
        } else if (identityType instanceof FileGroup) {
            FileGroup group = (FileGroup) identityType;
            FileGroup fileGroup = (FileGroup) getGroup(ctx, group.getName());

            if (fileGroup != null) {
                return fileGroup.getAttributes();
            }

            flushRoles();
        } else {
            throwsNotSupportedIdentityType(identityType);
        }

        return null;
    }*/
    
    @Override
    public boolean validateCredential(User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential passwordCredential = (PasswordCredential) credential;

            User storedUser = getUser(user.getId());
            String storedPassword = storedUser.<String>getAttribute(USER_PASSWORD_ATTRIBUTE).getValue();

            return storedPassword != null && storedPassword.equals(passwordCredential.getPassword());
        } else if (credential instanceof DigestCredential) {
            DigestCredential digestCredential = (DigestCredential) credential;
            
            User storedUser = getUser(user.getId());
            String storedPassword = storedUser.<String>getAttribute(USER_PASSWORD_ATTRIBUTE).getValue();
            
            return DigestCredentialUtil.matchCredential(digestCredential, storedPassword.toCharArray());
        } else if (credential instanceof X509CertificateCredential) {
            X509CertificateCredential certCredential =  (X509CertificateCredential) credential;
            
            User storedUser = getUser(user.getId());
            
            String storedCert = storedUser.<String>getAttribute(USER_CERTIFICATE_ATTRIBUTE).getValue();
            
            if (storedCert != null) {
                try {
                    return storedCert.equals(new String(Base64.encodeBytes(certCredential.getCertificate().getEncoded())));
                } catch (CertificateEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throwsNotSupportedCredentialType(credential);
        }

        return false;
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential passwordCredential = (PasswordCredential) credential;

            User storedUser = getUser(user.getId());

            storedUser.setAttribute(new Attribute<String>(USER_PASSWORD_ATTRIBUTE, passwordCredential.getPassword()));
            
            flushUsers();
        } else if (credential instanceof X509CertificateCredential) {
            X509CertificateCredential certCredential =  (X509CertificateCredential) credential;
            
            User storedUser = getUser(user.getId());

            try {
                storedUser.setAttribute(new Attribute<String>(USER_CERTIFICATE_ATTRIBUTE, 
                        new String(Base64.encodeBytes(certCredential.getCertificate().getEncoded()))));
            } catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            throwsNotSupportedCredentialType(credential);
        }
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    /**
     * <p>
     * Sets the base directory which will be used to store informations.
     * </p>
     * 
     * @param workingDir
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * <p>
     * Indicates that the files must be always recreated during the initialization.
     * </p>
     * 
     * @param alwaysCreateFiles
     */
    public void setAlwaysCreateFiles(boolean alwaysCreateFiles) {
        this.alwaysCreateFiles = alwaysCreateFiles;
    }
    
    private void searchForIdentityTypeAttributes(List<? extends IdentityType> users, Map<String, String[]> queryAttributes) {
        if (queryAttributes != null) {
            Set<Entry<String, String[]>> entrySet = queryAttributes.entrySet();

            for (IdentityType fileUser : new ArrayList<IdentityType>(users)) {
                for (Entry<String, String[]> entry : entrySet) {
                    String searchAttributeKey = entry.getKey();
                    String[] searchAttributeValue = entry.getValue();

                    String[] userAttributes = fileUser.<String[]>getAttribute(searchAttributeKey).getValue();

                    if (userAttributes == null) {
                        users.remove(fileUser);
                        continue;
                    }

                    if (Collections.indexOfSubList(Arrays.asList(userAttributes), Arrays.asList(searchAttributeValue)) > 0) {
                        users.remove(fileUser);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Helper method to throws a {@link IllegalArgumentException} when the specified {@link Credential} is not supported.
     * </p>
     * TODO: when using JBoss Logging this method should be removed.
     * 
     * @param credential
     * @return
     */
    private void throwsNotSupportedCredentialType(Credential credential) throws IllegalArgumentException {
        throw new IllegalArgumentException("Credential type not supported: " + credential.getClass());
    }

    /**
     * <p>
     * Helper method to throws a {@link IllegalArgumentException} when the specified {@link IdentityType} is not supported.
     * </p>
     * TODO: when using JBoss Logging this method should be removed.
     * 
     * @param credential
     * @return
     */
    private void throwsNotSupportedIdentityType(IdentityType identityType) throws IllegalArgumentException {
        throw new IllegalArgumentException("IdentityType not supported: " + identityType.getClass());
    }

    @Override
    public void updateUser(User user) {
        // TODO implement this

    }
    @Override
    public void setup(IdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public IdentityStoreConfiguration getConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Attribute getAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IdentityType> fetchQueryResults(Map<QueryParameter, Object> parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateRole(Role role) {
        // TODO Auto-generated method stub
        
    }

}
