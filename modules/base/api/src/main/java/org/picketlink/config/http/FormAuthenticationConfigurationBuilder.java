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
 * <p>Configures HTTP FORM Authentication for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class FormAuthenticationConfigurationBuilder extends AuthenticationMethodConfigurationBuilder {

    private String loginPageUrl;
    private String errorPageUrl;
    private boolean restoreOriginalRequest;

    FormAuthenticationConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    /**
     * <p>Indicates if the original request should be restored after a successful authentication. The original request is usually
     * the last one that originatly initiated the authentication process.</p>
     *
     * @return
     */
    public FormAuthenticationConfigurationBuilder restoreOriginalRequest() {
        this.restoreOriginalRequest = true;
        return this;
    }

    /**
     * <p>Specifies the login page url. If the user is not authenticated the give url will be used to redirect the user to.</p>
     *
     * @param loginPageUrl
     * @return
     */
    public FormAuthenticationConfigurationBuilder loginPage(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
        return this;
    }

    /**
     * <p>Specifies the login error page url. If the user is not authenticated after right after an authentication attepmt
     * the give url will be used to redirect the user to.</p>
     *
     * @param errorPageUrl
     * @return
     */
    public FormAuthenticationConfigurationBuilder errorPage(String errorPageUrl) {
        this.errorPageUrl = errorPageUrl;
        return this;
    }

    @Override
    AuthenticationSchemeConfiguration create(AuthenticationConfiguration authenticationConfiguration) {
        return new FormAuthenticationConfiguration(this.loginPageUrl, this.errorPageUrl, this.restoreOriginalRequest, authenticationConfiguration);
    }
}
