/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.idm.ldap.internal;

import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.spi.SecurityContext;

/**
 * This particular implementation supports the validation of UsernamePasswordCredentials, and updating PlainTextPassword
 * credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ UsernamePasswordCredentials.class, Password.class })
public class LDAPPlainTextPasswordCredentialHandler<S, V, U>
    implements CredentialHandler<LDAPIdentityStore, UsernamePasswordCredentials, Password> {


    public void update(org.picketlink.idm.spi.SecurityContext context, org.picketlink.idm.model.sample.Agent agent, org.picketlink.idm.credential.Password credential, org.picketlink.idm.ldap.internal.LDAPIdentityStore store, java.util.Date effectiveDate, java.util.Date expiryDate) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void validate(SecurityContext context, UsernamePasswordCredentials usernamePassword,
            LDAPIdentityStore identityStore) {
        usernamePassword.setStatus(Status.INVALID);
        usernamePassword.setValidatedAgent(null);

        Agent agent = identityStore.getAgent(context, usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            if (agent.isEnabled()) {
                LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) identityStore;
                LDAPUser ldapUser = ldapIdentityStore.lookupEntryById(context, LDAPUser.class, agent.getId());
                char[] password = usernamePassword.getPassword().getValue();

                boolean isValid = ldapIdentityStore.getLDAPManager().authenticate(ldapUser.getDN(), new String(password));

                if (isValid) {
                    usernamePassword.setValidatedAgent(agent);
                    usernamePassword.setStatus(Status.VALID);
                }
            } else {
                usernamePassword.setStatus(Status.AGENT_DISABLED);
            }
        }
    }

    @Override
    public void setup(org.picketlink.idm.ldap.internal.LDAPIdentityStore store) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";
//
//    @Override
//    public void setup(LDAPIdentityStore store) {
//    }
//
//    @Override
//    public void validate(SecurityContext context, UsernamePasswordCredentials usernamePassword,
//            LDAPIdentityStore identityStore) {
//        usernamePassword.setStatus(Status.INVALID);
//
//        Agent agent = identityStore.getAgent(context, usernamePassword.getUsername());
//
//        // If the user for the provided username cannot be found we fail validation
//        if (agent != null) {
//            if (agent.isEnabled()) {
//                LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) identityStore;
//                LDAPUser ldapUser = ldapIdentityStore.lookupEntryById(context, LDAPUser.class, agent.getId());
//                char[] password = usernamePassword.getPassword().getValue();
//
//                boolean isValid = ldapIdentityStore.getLDAPManager().authenticate(ldapUser.getDN(), new String(password));
//
//                if (isValid) {
//                    usernamePassword.setStatus(Status.VALID);
//                }
//            } else {
//                usernamePassword.setStatus(Status.AGENT_DISABLED);
//            }
//        }
//    }
//
//    @Override
//    public void update(SecurityContext context, Agent agent, Password password, LDAPIdentityStore store,
//            Date effectiveDate, Date expiryDate) {
//
//        LDAPUser ldapuser = store.lookupEntryById(context, LDAPUser.class, agent.getId());
//
//        if (store.getConfig().isActiveDirectory()) {
//            updateADPassword(ldapuser, new String(password.getValue()), store);
//        } else {
//            ModificationItem[] mods = new ModificationItem[1];
//
//            try {
//                Attribute mod0 = new BasicAttribute(USER_PASSWORD_ATTRIBUTE, new String(password.getValue()));
//
//                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
//
//                store.getLDAPManager().modifyAttribute(ldapuser.getDN(), mod0);
//            } catch (Exception e) {
//                throw new IdentityManagementException("Error updating password.", e);
//            }
//        }
//    }
//
//    private void updateADPassword(LDAPUser user, String password, LDAPIdentityStore store) {
//        try {
//            // Replace the "unicdodePwd" attribute with a new value
//            // Password must be both Unicode and a quoted string
//            String newQuotedPassword = "\"" + password + "\"";
//            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
//
//            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);
//
//            store.getLDAPManager().modifyAttribute(user.getDN(), unicodePwd);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
