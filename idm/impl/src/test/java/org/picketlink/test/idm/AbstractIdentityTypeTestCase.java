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
        T enabledIdentityTypeInstance = getIdentityType(true);

        assertTrue(enabledIdentityTypeInstance.isEnabled());

        enabledIdentityTypeInstance.setEnabled(false);

        updateIdentityType(enabledIdentityTypeInstance);

        T disabledIdentityTypeInstance = getIdentityType(false);

        assertFalse(disabledIdentityTypeInstance.isEnabled());

        disabledIdentityTypeInstance.setEnabled(true);

        updateIdentityType(disabledIdentityTypeInstance);

        enabledIdentityTypeInstance = getIdentityType(false);

        assertTrue(enabledIdentityTypeInstance.isEnabled());
    }

    protected abstract void updateIdentityType(T identityTypeInstance);

    protected abstract T getIdentityType(boolean alwaysCreate);

    /**
     * <p>
     * Expires an user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExpiration() throws Exception {
        T validIdentityTypeInstance = getIdentityType(true);

        Date expirationDate = new Date();

        validIdentityTypeInstance.setExpirationDate(expirationDate);

        updateIdentityType(validIdentityTypeInstance);

        T expiredIdentityTypeInstance = getIdentityType(false);

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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("one-valued", "1"));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals("1", multiValuedAttribute.getValue()[0]);
        assertEquals("2", multiValuedAttribute.getValue()[1]);
        assertEquals("3", multiValuedAttribute.getValue()[2]);
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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityTypeInstance.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedIdentityTypeInstance.setAttribute(multiValuedAttribute);

        updateIdentityType(updatedIdentityTypeInstance);

        updatedIdentityTypeInstance = getIdentityType(false);

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
        T storedIdentityTypeInstance = getIdentityType(true);

        storedIdentityTypeInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        updateIdentityType(storedIdentityTypeInstance);

        T updatedIdentityTypeInstance = getIdentityType(false);

        Attribute<String[]> multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityTypeInstance.removeAttribute("multi-valued");

        updateIdentityType(updatedIdentityTypeInstance);

        updatedIdentityTypeInstance = getIdentityType(false);

        multiValuedAttribute = updatedIdentityTypeInstance.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

}
