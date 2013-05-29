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

import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.QueryParameter;

/**
 * <p>Default {@link IdentityType} implementation  to represent agents.</p>
 *
 * @author Shane Bryzak
 */
public class Agent extends AbstractIdentityType {

    private static final long serialVersionUID = -7418037050013416323L;

    /**
     *  A query parameter used to set the key value.
     */
    public static final QueryParameter LOGIN_NAME = new QueryParameter() {};

    private String loginName;

    public Agent(String loginName) {
        this.loginName = loginName;
    }

    @AttributeProperty
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
