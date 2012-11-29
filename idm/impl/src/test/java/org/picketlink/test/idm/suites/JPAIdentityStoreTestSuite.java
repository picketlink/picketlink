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

package org.picketlink.test.idm.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.test.idm.GroupManagementTestCase;
import org.picketlink.test.idm.RoleManagementTestCase;
import org.picketlink.test.idm.UserManagementTestCase;
import org.picketlink.test.idm.runners.IdentityManagerRunner;
import org.picketlink.test.idm.runners.TestLifecycle;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ UserManagementTestCase.class, RoleManagementTestCase.class, GroupManagementTestCase.class})
public class JPAIdentityStoreTestSuite implements TestLifecycle{

    public static TestLifecycle init() throws Exception {
        return new JPAIdentityStoreTestSuite();
    }
    
    @Override
    public void onInit() {
        // TODO: put here the initialization logic. This method will be called before each test method.
    }

    @Override
    public IdentityManager createIdentityManager() {
        // TODO: put here the logic that creates an IdentityManager instance with the configured store.
        return null;
    }

    @Override
    public void onDestroy() {
     // TODO: put here the initialization logic. This method will be called after each test method.
    }

}
