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
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractIdentityTypeTestCase<T extends IdentityType> extends AbstractIdentityManagerTestCase {

    @Test
    public void testDisable() throws Exception {
        T enabledIdentityType = createIdentityType();

        assertTrue(enabledIdentityType.isEnabled());

        enabledIdentityType.setEnabled(false);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(enabledIdentityType);

        T disabledIdentityType = getIdentityType();

        assertFalse(disabledIdentityType.isEnabled());

        disabledIdentityType.setEnabled(true);

        identityManager.update(disabledIdentityType);

        enabledIdentityType = getIdentityType();

        assertTrue(enabledIdentityType.isEnabled());
    }
    
    @Test
    public void testLookupById() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        T identityType = createIdentityType();
        
        T lookedUpIdentityType = identityManager.lookupIdentityById((Class<T>) identityType.getClass(), identityType.getId());

        assertNotNull(identityType);
        assertEquals(identityType.getId(), lookedUpIdentityType.getId());
        
        // should also be possible to lookup all IdentityType instances
        lookedUpIdentityType = (T) identityManager.lookupIdentityById(IdentityType.class, identityType.getId());

        assertNotNull(lookedUpIdentityType);
        assertEquals(identityType.getId(), lookedUpIdentityType.getId());

        assertNull(identityManager.lookupIdentityById(identityType.getClass(), "bad_id"));
    }
    
    protected abstract T createIdentityType();
    
    protected abstract T getIdentityType();

    @Test
    public void testExpiration() throws Exception {
        T validIdentityType = createIdentityType();

        Date expirationDate = new Date();

        validIdentityType.setExpirationDate(expirationDate);

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(validIdentityType);

        T expiredIdentityType = getIdentityType();
        
        assertNotNull(expiredIdentityType.getExpirationDate());
        assertTrue(expirationDate.compareTo(expiredIdentityType.getExpirationDate()) == 0);
    }

    @Test
    public void testSetOneValuedAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String>("one-valued", "1"));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String> oneValuedAttribute = updatedIdentityType.getAttribute("one-valued");

        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }

    @Test
    public void testSetMultiValuedAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertNotNull(multiValuedAttribute.getValue());
        assertEquals(3, multiValuedAttribute.getValue().length);
        
        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);
        
        assertTrue(Arrays.equals(values, new String[] { "1", "2", "3" }));
    }

    @Test
    public void testSetMultipleAttributes() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        assertEquals("2", updatedIdentityType.<String> getAttribute("QuestionTotal").getValue());
        assertEquals("What is favorite toy?", updatedIdentityType.<String> getAttribute("Question1").getValue());
        assertEquals("Gum", updatedIdentityType.<String> getAttribute("Question1Answer").getValue());
        assertEquals("What is favorite word?", updatedIdentityType.<String> getAttribute("Question2").getValue());
        assertEquals("Hi", updatedIdentityType.<String> getAttribute("Question2Answer").getValue());
    }

    @Test
    public void testGetAllAttributes() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));

        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Collection<Attribute<? extends Serializable>> allAttributes = updatedIdentityType.getAttributes();

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

    @Test
    public void testUpdateAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedIdentityType.setAttribute(multiValuedAttribute);

        identityManager.update(updatedIdentityType);

        updatedIdentityType = getIdentityType();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);
        
        assertTrue(Arrays.equals(values, new String[] { "3", "4", "5" }));
    }

    @Test
    public void testRemoveAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityType.removeAttribute("multi-valued");
        
        identityManager.update(updatedIdentityType);

        updatedIdentityType = getIdentityType();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

    @Test (expected=IdentityManagementException.class)
    public void testAddDuplicatedObject() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setId(null);

        getIdentityManager().add(storedIdentityType);
    }

}
