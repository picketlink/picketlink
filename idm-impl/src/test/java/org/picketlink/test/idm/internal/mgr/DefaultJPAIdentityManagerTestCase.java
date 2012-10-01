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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.JPAIdentityStore;
import org.picketlink.idm.internal.password.SHASaltedPasswordEncoder;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.test.idm.internal.jpa.AbstractJPAIdentityManagerTestCase;

/**
 * Unit test the {@link DefaultIdentityManager} using the {@link JPAIdentityStore}
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */
public class DefaultJPAIdentityManagerTestCase extends AbstractJPAIdentityManagerTestCase {

    /**
     * <p>
     * Tests a basic {@link IdentityManager} usage workflow.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testDefaultIdentityManager() throws Exception {

        DefaultIdentityManager im = createIdentityManager();

        // Let us create an user
        User user = im.createUser("pedroigor");

        user.setFirstName("Pedro");
        user.setLastName("Igor");

        assertNotNull(user);

        user = im.getUser("pedroigor");
        assertNotNull(user);
        assertEquals("Pedro Igor", user.getFullName());
        assertEquals("Pedro", user.getFirstName());
        assertEquals("Igor", user.getLastName());

        // Deal with user's attributes
        user.setAttribute("QuestionTotal", "2");
        user.setAttribute("Question1", "What is favorite toy?");
        user.setAttribute("Question1Answer", "Gum");

        user.setAttribute("Question2", "What is favorite word?");
        user.setAttribute("Question2Answer", "Hi");

        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/servercert.txt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
        bis.close();

        String encodedCert = Base64.encodeBytes(cert.getEncoded());
        user.setAttribute("x509", encodedCert);

        // let us retrieve the attributes from ldap store and see if they are the same
        user = im.getUser("pedroigor");
        Map<String, String[]> attributes = user.getAttributes();
        assertNotNull(attributes);

        assertEquals("2", attributes.get("QuestionTotal")[0]);
        assertEquals("What is favorite toy?", attributes.get("Question1")[0]);
        assertEquals("Gum", attributes.get("Question1Answer")[0]);
        assertEquals("What is favorite word?", attributes.get("Question2")[0]);
        assertEquals("Hi", attributes.get("Question2Answer")[0]);

        String loadedCert = attributes.get("x509")[0];
        byte[] certBytes = Base64.decode(loadedCert);

        cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        assertNotNull(cert);

        // Change password
        String userpass = "testpass";
        im.updatePassword(user, userpass);

        // Let us validate
        assertTrue(im.validatePassword(user, userpass));

        assertFalse(im.validatePassword(user, "BAD"));

        // Let us do UserQuery search
        UserQuery query = im.createUserQuery().setAttributeFilter("QuestionTotal", new String[] { "2" });

        List<User> returnedUsers = query.executeQuery();
        assertNotNull(returnedUsers);
        assertEquals(1, returnedUsers.size());

        Role adminRole = im.createRole("admin");
        Group testGroup = im.createGroup("Test Group");

        im.grantRole(adminRole, user, testGroup);

        Collection<Role> rolesByUser = im.getRoles(user, null);

        assertNotNull(rolesByUser);
        assertEquals(1, rolesByUser.size());

        Collection<Role> rolesByUserAndGroup = im.getRoles(user, testGroup);

        assertNotNull(rolesByUserAndGroup);
        assertEquals(1, rolesByUserAndGroup.size());

        im.removeUser(user);
        user = im.getUser("pedroigor");
        assertNull(user);
    }

    /**
     * <p>
     * Tests the configuration of {@link SHASaltedPasswordEncoder} to encode passwords.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testPasswordEncoding() throws Exception {
        DefaultIdentityManager identityManager = createIdentityManager();

        identityManager.setPasswordEncoder(new SHASaltedPasswordEncoder(256));

        // Let us create an user
        User user = identityManager.createUser("pedroigor");
        String password = "easypassword";

        identityManager.updatePassword(user, password);

        assertTrue(identityManager.validatePassword(user, password));
        
        identityManager.removeUser(user);
    }

    private DefaultIdentityManager createIdentityManager() {
        DefaultIdentityManager im = new DefaultIdentityManager();
        im.setIdentityStore(createIdentityStore()); // TODO: wiring needs a second look
        return im;
    }

}