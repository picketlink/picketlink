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

package org.picketlink.test.idm.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.AbstractJPADeploymentTestCase;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * @author Pedro Igor
 *
 */
public class AttributeManagementTestCase extends AbstractJPADeploymentTestCase {
    
    @Inject
    private IdentityManager identityManager;

    @Deployment
    public static WebArchive deploy() {
        return deploy(AttributeManagementTestCase.class);
    }
    
    @Test
    public void testAttribute() throws Exception {
        User john = new User("john");

        john.setAttribute(new Attribute<String>("attribute1", "value1"));

        this.identityManager.add(john);

        Role tester = new Role("Tester");

        tester.setAttribute(new Attribute<String>("attribute2", "value2"));

        this.identityManager.add(tester);

        Group qaGroup = new Group("QA");

        qaGroup.setAttribute(new Attribute<String>("attribute3", "value3"));

        this.identityManager.add(qaGroup);

        john = BasicModel.getUser(this.identityManager, john.getLoginName());

        assertNotNull(john.getAttribute("attribute1"));
        assertEquals("value1", john.getAttribute("attribute1").getValue());

        tester = BasicModel.getRole(this.identityManager, tester.getName());

        assertNotNull(tester.getAttribute("attribute2"));
        assertEquals("value2", tester.getAttribute("attribute2").getValue());

        qaGroup = BasicModel.getGroup(this.identityManager, qaGroup.getName());

        assertNotNull(qaGroup.getAttribute("attribute3"));
        assertEquals("value3", qaGroup.getAttribute("attribute3").getValue());

        john.removeAttribute("attribute1");

        this.identityManager.update(john);

        john = BasicModel.getUser(this.identityManager, john.getLoginName());

        assertNull(john.getAttribute("attribute1"));

        tester = BasicModel.getRole(this.identityManager, tester.getName());

        assertNotNull(tester.getAttribute("attribute2"));
        assertEquals("value2", tester.getAttribute("attribute2").getValue());

        qaGroup = BasicModel.getGroup(this.identityManager, qaGroup.getName());

        assertNotNull(qaGroup.getAttribute("attribute3"));
        assertEquals("value3", qaGroup.getAttribute("attribute3").getValue());

        qaGroup.getAttribute("attribute3").setValue("newValue");

        identityManager.update(qaGroup);

        qaGroup = BasicModel.getGroup(this.identityManager, qaGroup.getName());

        assertEquals("newValue", qaGroup.getAttribute("attribute3").getValue());

        tester.setAttribute(new Attribute<Long>("age", 40l));

        identityManager.update(tester);

        tester = BasicModel.getRole(this.identityManager, tester.getName());

        assertNotNull(tester.getAttribute("age"));
        assertEquals(40l, tester.getAttribute("age").getValue());
    }

}