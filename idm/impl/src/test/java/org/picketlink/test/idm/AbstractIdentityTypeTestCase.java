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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractIdentityTypeTestCase<T extends IdentityType> extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Disables an user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testDisable() throws Exception {
        T enabledIdentityTypeInstance = createIdentityType();

        assertTrue(enabledIdentityTypeInstance.isEnabled());

        enabledIdentityTypeInstance.setEnabled(false);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(enabledIdentityTypeInstance);

        T disabledIdentityTypeInstance = getIdentityType();

        assertFalse(disabledIdentityTypeInstance.isEnabled());

        disabledIdentityTypeInstance.setEnabled(true);

        identityManager.update(disabledIdentityTypeInstance);

        enabledIdentityTypeInstance = getIdentityType();

        assertTrue(enabledIdentityTypeInstance.isEnabled());
    }
    
    @Test
    public void testLookupById() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        T identityType = createIdentityType();
        
        T lookedUpIdentityType = identityManager.lookupIdentityById((Class<T>)identityType.getClass(), identityType.getId());

        assertNotNull(identityType);
        assertEquals(identityType.getId(), lookedUpIdentityType.getId());
        
        assertNull(identityManager.lookupIdentityById(identityType.getClass(), "bad_id"));
    }
    
    protected abstract T createIdentityType();
    
    protected abstract T getIdentityType();

    /**
     * <p>
     * Expires an user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExpiration() throws Exception {
        T validIdentityTypeInstance = createIdentityType();

        Date expirationDate = new Date();

        validIdentityTypeInstance.setExpirationDate(expirationDate);

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(validIdentityTypeInstance);

        T expiredIdentityTypeInstance = getIdentityType();
        
        Thread.sleep(500);
        
        assertNotNull(expiredIdentityTypeInstance.getExpirationDate());
        assertTrue(new Date().after(expiredIdentityTypeInstance.getExpirationDate()));
        assertTrue(expirationDate.compareTo(expiredIdentityTypeInstance.getExpirationDate()) == 0);
    }

    /**
     * <p>
     * Sets an one-valued attribute.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testSetOneValuedAttribute() throws Exception {
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("one-valued", "1"));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        Attribute<String> oneValuedAttribute = updatedIdentityTypeInstance.getAttribute("one-valued");

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
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertNotNull(multiValuedAttribute.getValue());
        assertEquals(3, multiValuedAttribute.getValue().length);
    }

    /**
     * <p>
     * Sets multiple attributes and check if they are properly stored.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testSetMultipleAttributes() throws Exception {
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2Answer", "Hi"));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        assertEquals("2", updatedIdentityTypeInstance.<String> getAttribute("QuestionTotal").getValue());
        assertEquals("What is favorite toy?", updatedIdentityTypeInstance.<String> getAttribute("Question1").getValue());
        assertEquals("Gum", updatedIdentityTypeInstance.<String> getAttribute("Question1Answer").getValue());
        assertEquals("What is favorite word?", updatedIdentityTypeInstance.<String[]> getAttribute("Question2").getValue());
        assertEquals("Hi", updatedIdentityTypeInstance.<String> getAttribute("Question2Answer").getValue());
    }

    /**
     * <p>
     * Gets all stored attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGetAllAttributes() throws Exception {
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        Collection<Attribute<? extends Serializable>> allAttributes = updatedIdentityTypeInstance.getAttributes();

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
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedIdentityTypeInstance.setAttribute(multiValuedAttribute);

        identityManager.update(updatedIdentityTypeInstance);

        updatedIdentityTypeInstance = getIdentityType();

        multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);
        assertTrue(Arrays.equals(values, new String[] { "3", "4", "5" }));
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
        T storedIdentityTypeInstance = createIdentityType();

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityTypeInstance.removeAttribute("multi-valued");
        
        identityManager.update(updatedIdentityTypeInstance);

        updatedIdentityTypeInstance = getIdentityType();

        multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

}
