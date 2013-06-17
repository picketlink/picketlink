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

package org.picketlink.idm.model.sample;

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.RelationshipQueryParameter;

/**
 * Models an oAuth authorization
 *
 * @author Shane Bryzak
 *
 */
public class Authorization extends AbstractAttributedType implements Relationship {

    private static final long serialVersionUID = -8044173562668371515L;

    public static final RelationshipQueryParameter USER = new RelationshipQueryParameter() {

        @Override
        public String getName() {
            return "user";
        }
    };;

    public static final RelationshipQueryParameter APPLICATION = new RelationshipQueryParameter() {

        @Override
        public String getName() {
            return "application";
        }
    };;

    private User user;
    private Agent application;

    private String authorizationCode;
    private String accessToken;
    private String refreshToken;

    public Authorization() {
        super();
    }

    public Authorization(User user, Agent application) {
        this.user = user;
        this.application = application;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Agent getApplication() {
        return application;
    }

    public void setApplication(Agent application) {
        this.application = application;
    }

    @AttributeProperty
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    @AttributeProperty
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @AttributeProperty
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
