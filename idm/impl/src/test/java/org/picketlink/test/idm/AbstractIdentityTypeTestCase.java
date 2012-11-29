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

package org.picketlink.test.idm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Collection;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.User;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractIdentityTypeTestCase {

    private IdentityManager identityManager;
    
    /**
     * <p>
     * Sets an one-valued attribute.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetOneValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String>("one-valued", "1"));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());

        Attribute<String> oneValuedAttribute = updatedUserInstance.getAttribute("one-valued");

        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }

    /**
     * <p>
     * Sets a multi-valued attribute.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetMultiValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());

        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals("1", multiValuedAttribute.getValue()[0]);
        assertEquals("2", multiValuedAttribute.getValue()[1]);
        assertEquals("3", multiValuedAttribute.getValue()[2]);
    }
    
    /**
     * <p>Sets multiple attributes and check if they are properly stored.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetMultipleAttributes() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String[]>("QuestionTotal", new String[] { "2" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question1", new String[] { "What is favorite toy?" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question1Answer", new String[] { "Gum" }));

        storedUserInstance.setAttribute(new Attribute<String[]>("Question2", new String[] { "What is favorite word?" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question2Answer", new String[] { "Hi" }));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        assertEquals("2", updatedUserInstance.<String[]>getAttribute("QuestionTotal").getValue()[0]);
        assertEquals("What is favorite toy?", updatedUserInstance.<String[]>getAttribute("Question1").getValue()[0]);
        assertEquals("Gum", updatedUserInstance.<String[]>getAttribute("Question1Answer").getValue()[0]);
        assertEquals("What is favorite word?", updatedUserInstance.<String[]>getAttribute("Question2").getValue()[0]);
        assertEquals("Hi", updatedUserInstance.<String[]>getAttribute("Question2Answer").getValue()[0]);
    }
    
    /**
     * <p>Gets all stored attributes.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGetAllAttributes() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String[]>("QuestionTotal", new String[] { "2" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question1", new String[] { "What is favorite toy?" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question1Answer", new String[] { "Gum" }));

        storedUserInstance.setAttribute(new Attribute<String[]>("Question2", new String[] { "What is favorite word?" }));
        storedUserInstance.setAttribute(new Attribute<String[]>("Question2Answer", new String[] { "Hi" }));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        Collection<Attribute<? extends Serializable>> allAttributes = updatedUserInstance.getAttributes();
        
        assertFalse(allAttributes.isEmpty());
        
        boolean hasQuestionTotal = false;
        boolean hasQuestion1 = false;
        boolean hasQuestion1Answer = false;
        boolean hasQuestion2 = false;
        boolean hasQuestion2Answer = false;
        
        for (Attribute<? extends Serializable> attribute : allAttributes) {
            if (attribute.getName().equals("QuestionTotal")) {
                hasQuestionTotal = true;
            }
            if (attribute.getName().equals("Question1")) {
                hasQuestion1 = true;
            }
            if (attribute.getName().equals("Question1Answer")) {
                hasQuestion1Answer = true;
            }
            if (attribute.getName().equals("Question2")) {
                hasQuestion2 = true;
            }
            if (attribute.getName().equals("Question2Answer")) {
                hasQuestion2Answer = true;
            }
        }
        
        assertTrue(hasQuestionTotal);
        assertTrue(hasQuestion1);
        assertTrue(hasQuestion1Answer);
        assertTrue(hasQuestion2);
        assertTrue(hasQuestion2Answer);
    }


    /**
     * <p>
     * Updates an attribute.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());

        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedUserInstance.setAttribute(multiValuedAttribute);

        identityManager.updateUser(updatedUserInstance);

        updatedUserInstance = identityManager.getUser("admin");

        multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals("3", multiValuedAttribute.getValue()[0]);
        assertEquals("4", multiValuedAttribute.getValue()[1]);
        assertEquals("5", multiValuedAttribute.getValue()[2]);
    }

    /**
     * <p>
     * Removes an attribute.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.updateUser(storedUserInstance);

        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());

        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedUserInstance.removeAttribute("multi-valued");

        identityManager.updateUser(updatedUserInstance);

        updatedUserInstance = identityManager.getUser("admin");

        multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }
    
    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

}
