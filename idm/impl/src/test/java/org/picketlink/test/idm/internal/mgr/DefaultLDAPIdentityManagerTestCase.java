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
package org.picketlink.test.idm.internal.mgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * Unit test the {@link DefaultIdentityManager} using the {@link LDAPIdentityStore}.
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */
public class DefaultLDAPIdentityManagerTestCase extends AbstractLDAPTest {

    @Before
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    @Test @Ignore
    public void testDefaultIdentityManager() throws Exception {
        IdentityConfiguration config = new IdentityConfiguration();
        config.addStoreConfiguration(getConfiguration());

        DefaultIdentityManager im = new DefaultIdentityManager();
        im.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));

        // Let us create an user
        User user = new SimpleUser("asaldhan");
        im.add(user);

        assertNotNull(user);

        user.setFirstName("Anil");
        user.setLastName("Saldhana");

        User anil = im.getUser("asaldhan");

        assertNotNull(anil);

//        assertEquals("Anil Saldhana", anil.getFullName());
        assertEquals("Anil", anil.getFirstName());
        assertEquals("Saldhana", anil.getLastName());

        // Deal with Anil's attributes
        anil.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        anil.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        anil.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        anil.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        anil.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/servercert.txt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
        bis.close();

        String encodedCert = Base64.encodeBytes(cert.getEncoded());
        anil.setAttribute(new Attribute<String>("x509", encodedCert));

        // Try saving the cert as standard ldap cert
        im.updateCredential(anil, new X509CertificateCredential(cert));

        // let us retrieve the attributes from ldap store and see if they are the same
        anil = im.getUser("asaldhan");
        assertNotNull(anil.getAttributes());

        // Can we still get the cert as an attribute?
        String strCert = anil.<String>getAttribute("usercertificate").getValue();
        byte[] decodedCert = Base64.decode(strCert);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(decodedCert);
        X509Certificate newCert = (X509Certificate) cf.generateCertificate(byteStream);
        assertNotNull(newCert);

        assertEquals("2", anil.<String[]>getAttribute("QuestionTotal").getValue()[0]);
        assertEquals("What is favorite toy?", anil.<String[]>getAttribute("Question1").getValue()[0]);
        assertEquals("Gum", anil.<String[]>getAttribute("Question1Answer").getValue()[0]);
        assertEquals("What is favorite word?", anil.<String[]>getAttribute("Question2").getValue()[0]);
        assertEquals("Hi", anil.<String[]>getAttribute("Question2Answer").getValue()[0]);

        String loadedCert = anil.<String[]>getAttribute("x509").getValue()[0];
        byte[] certBytes = Base64.decode(loadedCert);

        cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        assertNotNull(cert);

        // Change password
        String anilpass = "testpass";
        PasswordCredential pc = new PasswordCredential(anilpass.toCharArray());
        im.updateCredential(anil, pc);

        // Let us validate
        // FIXME
        //assertTrue(im.validateCredential(anil, pc));

        // FIXME
        //assertFalse(im.validateCredential(anil, new PasswordCredential("BAD".toCharArray())));

        // Let us do UserQuery search
        // FIXME rewrite with new Query API
        //UserQuery query = im.createUserQuery().setAttributeFilter("QuestionTotal", new String[] { "2" });

        //List<User> returnedUsers = query.executeQuery();
        //assertNotNull(returnedUsers);
        //assertEquals(1, returnedUsers.size());

        Role adminRole = new SimpleRole("admin");
        im.add(adminRole);

        Group testGroup = new SimpleGroup("Fake Group", null);
        im.add(testGroup);

        Group unusedGroup = new SimpleGroup("Unused Group", null);
        im.add(unusedGroup);

        // grant adminRole to anil and put the user in the testGroup
        im.grantGroupRole(anil, adminRole, testGroup);

        // get the roles for anil. We should have only adminRole
        // FIXME rewrite using Query API
        Collection<Role> rolesByUser = null; //im.getRoles(anil, null);

        assertNotNull(rolesByUser);
        assertEquals(1, rolesByUser.size());

        // get the roles for anil if the role is member of the testGroup. We should have only adminRole
        // FIXME rewrite using Query API
        Collection<Role> rolesByUserAndGroup = null; //im.getRoles(anil, testGroup);

        assertNotNull(rolesByUserAndGroup);
        assertEquals(1, rolesByUserAndGroup.size());

        // get the roles for anil if the role is member of unusedGroup. No role should be returned because only the testGroup is
        // associated with the adminRole
        // FIXME rewrite using Query API
        Collection<Role> emptyRolesForUnusedGroup = null; //im.getRoles(anil, unusedGroup);

        assertNotNull(emptyRolesForUnusedGroup);
        assertTrue(emptyRolesForUnusedGroup.isEmpty());

        im.remove(anil);

        anil = im.getUser("asaldhan");

        assertNull(anil);

    }

    private LDAPConfiguration getConfiguration() {
        String fqn = LDAPConfigurationBuilder.class.getName();
        LDAPConfiguration config = (LDAPConfiguration) IdentityStoreConfigurationBuilder.config(fqn);

        config.setBindDN(adminDN).setBindCredential(adminPW).setLdapURL("ldap://localhost:10389");
        config.setUserDNSuffix("ou=People,dc=jboss,dc=org").setRoleDNSuffix("ou=Roles,dc=jboss,dc=org");
        config.setGroupDNSuffix("ou=Groups,dc=jboss,dc=org");
        return config;
    }
}