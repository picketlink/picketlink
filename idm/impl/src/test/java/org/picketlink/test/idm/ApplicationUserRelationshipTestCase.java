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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Authorization;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * OAuth Use Case of an User X authorizing an OAuth application APP to have access 
 * to protected resources R with various scopes S.
 * 
 * The relationship should also store an access token T, authorization code AC and
 * a refresh token RT.
 * 
 * @author anil saldhana
 * @since Dec 18, 2012
 *
 */
public class ApplicationUserRelationshipTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void authorizeAccess() throws Exception{
        String authorizationCode = "ac";
        String accessToken = "at";
        String refreshToken = "rt";
        
        IdentityManager identityManager = getIdentityManager();
        
        //Create an User robert
        User robert = createUser("robert");
        
        //Create an OAuth application called "My OAuth App"
        Agent myOauthApp = new SimpleAgent("My OAuth App");

        identityManager.add(myOauthApp);
        
        Authorization authorized = new Authorization(robert, myOauthApp);
        
        authorized.setAttribute(new Attribute<String>("authorizationCode", authorizationCode));
        authorized.setAttribute(new Attribute<String>("accessToken", accessToken));
        authorized.setAttribute(new Attribute<String>("refreshToken", refreshToken));
        
        identityManager.add(authorized);
        
        //Query the relationship
        RelationshipQuery<Authorization> query = identityManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        List<Authorization> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        
        authorized = result.get(0);
        
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        assertNotNull(authorized.getAttribute("authorizationCode"));
        
        query = identityManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        
        query = identityManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        
        User someUser = createUser("someUser");
        
        query = identityManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, someUser);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        // user is not authorized to the application
        assertTrue(result.isEmpty());
        
        // remove the relationship
        identityManager.remove(authorized);
        
        query = identityManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        assertTrue(result.isEmpty());
    }
}