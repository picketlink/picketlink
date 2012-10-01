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

package org.picketlink.test.idm.internal.jpa;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.IdentityType;

/**
 * <p>Base class for {@link IdentityType} test cases.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractJPAIdentityTypeTestCase extends AbstractJPAIdentityManagerTestCase {

    /**
     * <p>
     * Tests the setup of some attributes for an {@link IdentityType}.
     * </p>
     *
     * @throws Exception
     */
    protected void testAddAttributes() throws Exception {
        IdentityType identityType = getIdentityTypeFromDatabase(getIdentityManager());

        identityType.setAttribute("QuestionTotal", "2");
        identityType.setAttribute("Question1", "What is favorite toy?");
        identityType.setAttribute("Question1Answer", "Gum");
        identityType.setAttribute("MultiValuedAttribute", new String[] { "value1", "value2", "value3" });

        assertEquals("2", identityType.getAttribute("QuestionTotal"));
        assertEquals("What is favorite toy?", identityType.getAttribute("Question1"));
        assertEquals("Gum", identityType.getAttribute("Question1Answer"));

        assertEquals("value1", identityType.getAttributeValues("MultiValuedAttribute")[0]);
        assertEquals("value2", identityType.getAttributeValues("MultiValuedAttribute")[1]);
        assertEquals("value3", identityType.getAttributeValues("MultiValuedAttribute")[2]);
    }

    protected abstract IdentityType getIdentityTypeFromDatabase(IdentityManager identityStore);

    /**
     * <p>
     * Tests the removal of some attributes for an {@link IdentityType}.
     * </p>
     *
     * @throws Exception
     */
    protected void testRemoveAttributes() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        IdentityType identityType = getIdentityTypeFromDatabase(identityManager);

        assertNotNull(identityType.getAttributeValues("MultiValuedAttribute"));

        identityType.removeAttribute("MultiValuedAttribute");

        assertNull(identityType.getAttributeValues("MultiValuedAttribute"));
    }

}
