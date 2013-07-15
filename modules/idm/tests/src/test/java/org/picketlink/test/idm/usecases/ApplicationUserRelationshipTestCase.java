/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.idm.usecases;

import java.util.List;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Authorization;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
@IgnoreTester(LDAPStoreConfigurationTester.class)
public class ApplicationUserRelationshipTestCase extends AbstractPartitionManagerTestCase {

    public ApplicationUserRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void authorizeAccess() throws Exception{
        String authorizationCode = "ac";
        String accessToken = "at";
        String refreshToken = "rt";
        
        IdentityManager identityManager = getIdentityManager();
        
        //Create an User robert
        User robert = createUser("robert");
        
        //Create an OAuth application called "My OAuth App"
        Agent myOauthApp = new Agent("My OAuth App");

        identityManager.add(myOauthApp);
        
        Authorization authorized = new Authorization(robert, myOauthApp);
        
        authorized.setAuthorizationCode(authorizationCode);
        authorized.setAccessToken(accessToken);
        authorized.setRefreshToken(refreshToken);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(authorized);
        
        //Query the relationship
        RelationshipQuery<Authorization> query = relationshipManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        List<Authorization> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        
        authorized = result.get(0);
        
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        assertNotNull(authorized.getAuthorizationCode());
        
        query = relationshipManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        
        query = relationshipManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertNotNull(authorized.getUser());
        assertNotNull(authorized.getApplication());
        
        User someUser = createUser("someUser");
        
        query = relationshipManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, someUser);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        // user is not authorized to the application
        assertTrue(result.isEmpty());
        
        // remove the relationship
        relationshipManager.remove(authorized);
        
        query = relationshipManager.createRelationshipQuery(Authorization.class);
        
        query.setParameter(Authorization.USER, robert);
        query.setParameter(Authorization.APPLICATION, myOauthApp);
        
        result = query.getResultList();
        
        assertTrue(result.isEmpty());
    }
}