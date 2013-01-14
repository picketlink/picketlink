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
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

/**
 * Unit test the OAuth Application Registration
 * @author anil saldhana
 * @since Dec 18, 2012
 */
public class ApplicationRegistrationTestCase extends AbstractIdentityManagerTestCase {
    
    private static final String appName = "Test App";
    private static final String appURL = "http://someurl";
    private static final String appDesc = "This is a nice app";
    
    @Test
    public void appRegister() throws Exception{
        IdentityManager identityManager = getIdentityManager();
        
        Agent oauthApp = loadOrCreateAgent(appName, true);
        
        oauthApp.setAttribute( new Attribute<String>("appURL", appURL) );
        oauthApp.setAttribute( new Attribute<String>("appDesc", appDesc) );
        
        identityManager.update(oauthApp);
        
        //Let us query
        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.ID, appName);

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(appName, result.get(0).getId());
        
        // let's query using only the appUrl attribute
        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("appURL"), new String[] { appURL });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(appName, result.get(0).getId());
        
        //Query with a wrong agent id
        
        query = identityManager.createIdentityQuery(Agent.class);
        query.setParameter(Agent.ID, "bogus");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    @Test
    public void appUnregister() throws Exception{
        IdentityManager identityManager = getIdentityManager();
        
        Agent oauthApp = loadOrCreateAgent(appName, true);
        
        oauthApp = identityManager.getAgent(appName);
        
        assertNotNull(oauthApp);
        
        identityManager.remove(oauthApp);
        
        oauthApp = identityManager.getAgent(appName);
        
        assertNull(oauthApp);
    }
}