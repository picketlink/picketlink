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
package org.picketlink.config.http;

/**
 * @author Pedro Igor
 */
public class FormAuthenticationConfiguration extends AbstractAuthenticationSchemeConfiguration implements AuthenticationSchemeConfiguration {

    private final String loginPageUrl;
    private final String errorPageUrl;
    private final boolean restoreOriginalRequest;

    public FormAuthenticationConfiguration(
        String loginPageUrl,
        String errorPageUrl,
        boolean restoreOriginalRequest,
        AuthenticationConfiguration authenticationConfiguration) {
        super(authenticationConfiguration);
        if (loginPageUrl == null) {
            loginPageUrl = "/login.html";
        }

        this.loginPageUrl = loginPageUrl;

        if (errorPageUrl == null) {
            errorPageUrl = "/error.html";
        }

        this.errorPageUrl = errorPageUrl;
        this.restoreOriginalRequest = restoreOriginalRequest;
    }

    public String getLoginPageUrl() {
        return this.loginPageUrl;
    }

    public String getErrorPageUrl() {
        return this.errorPageUrl;
    }

    public boolean isRestoreOriginalRequest() {
        return this.restoreOriginalRequest;
    }
}
