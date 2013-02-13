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

package org.picketlink.idm.model;

import org.picketlink.idm.query.QueryParameter;


/**
 * Represents an external entity that interacts with the application, such as a user
 * or a third party application
 *  
 * @author Shane Bryzak
 */
public interface Agent extends IdentityType {
    
    /**
     *  A query parameter used to set the key value.
     */
    QueryParameter LOGIN_NAME = new QueryParameter() {};

    /**
     * Returns the login name of this agent.  This value should be unique, as it is used
     * to identify the agent for authentication
     * 
     * @return
     */
    String getLoginName();
    
    void setLoginName(String loginName);
}
