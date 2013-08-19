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

package org.picketlink.test.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.picketlink.Identity;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.util.ArchiveUtils;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * <p>
 * Base class for test cases that requires authentication. By default, the {@link IdmAuthenticator} is used.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public abstract class AbstractAuthenticationTestCase extends AbstractArquillianTestCase {

    protected static final String USER_NAME = "john";
    protected static final String USER_PASSWORD = "mypasswd";

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private IdentityManager identityManager;

    public static WebArchive deploy(Class... classesToAdd) {
        List<Class> classes = new ArrayList<Class>();

        classes.add(AbstractAuthenticationTestCase.class);
        classes.add(AbstractArquillianTestCase.class);
        classes.addAll(Arrays.asList(classesToAdd));

        return ArchiveUtils.create(classes.toArray(new Class[classes.size()]));
    }

    @Before
    public void onSetup() {
        User user = getUser(this.identityManager, USER_NAME);

        if (user == null) {
            user = new User("john");

            this.identityManager.add(user);

            Password password = new Password("mypasswd");

            this.identityManager.updateCredential(user, password);
        }

        user.setEnabled(true);

        this.identityManager.update(user);
    }

    @After
    public void onFinish() {
        this.identity.logout();
    }

    protected Account getAccount() {
        return getUser(this.identityManager, USER_NAME);
    }

    protected Identity getIdentity() {
        return this.identity;
    }

    protected DefaultLoginCredentials getCredentials() {
        return this.credentials;
    }

    protected IdentityManager getIdentityManager() {
        return this.identityManager;
    }

}
