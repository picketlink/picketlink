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

import org.picketlink.http.authentication.HttpAuthenticationScheme;

/**
 * <p>Provides a set of options to configure authentication for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class AuthenticationConfigurationBuilder {

    private final PathConfigurationBuilder parentBuilder;
    private AuthenticationMethodConfigurationBuilder authenticationMethodConfigurationBuilder;

    public AuthenticationConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
    }

    /**
     * <p>Configures HTTP FORM Authentication for a specific path.</p>
     *
     * @return
     */
    public FormAuthenticationConfigurationBuilder form() {
        this.authenticationMethodConfigurationBuilder = new FormAuthenticationConfigurationBuilder(this.parentBuilder);
        return (FormAuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    /**
     * <p>Configures HTTP BASIC Authentication for a specific path.</p>
     *
     * @return
     */
    public BasicAuthenticationConfigurationBuilder basic() {
        this.authenticationMethodConfigurationBuilder = new BasicAuthenticationConfigurationBuilder(this.parentBuilder);
        return (BasicAuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    /**
     * <p>Configures HTTP DIGEST Authentication for a specific path.</p>
     *
     * @return
     */
    public DigestAuthenticationConfigurationBuilder digest() {
        this.authenticationMethodConfigurationBuilder = new DigestAuthenticationConfigurationBuilder(this.parentBuilder);
        return (DigestAuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    /**
     * <p>Configures HTTP CLIENT-CERT Authentication for a specific path.</p>
     *
     * @return
     */
    public X509AuthenticationConfigurationBuilder x509() {
        this.authenticationMethodConfigurationBuilder = new X509AuthenticationConfigurationBuilder(this.parentBuilder);
        return (X509AuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    /**
     * <p>Configures Token-based Authentication for a specific path.</p>
     *
     * @return
     */
    public TokenAuthenticationConfigurationBuilder token() {
        this.authenticationMethodConfigurationBuilder = new TokenAuthenticationConfigurationBuilder(this.parentBuilder);
        return (TokenAuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    /**
     * <p>Configures a specific {@link org.picketlink.http.authentication.HttpAuthenticationScheme}.</p>
     *
     * <p>You may use this method to provide your own authentication scheme.</p>
     *
     * @return
     */
    public CustomAuthenticationConfigurationBuilder scheme(Class<? extends HttpAuthenticationScheme> schemeType) {
        this.authenticationMethodConfigurationBuilder = new CustomAuthenticationConfigurationBuilder(schemeType, this.parentBuilder);
        return (CustomAuthenticationConfigurationBuilder) this.authenticationMethodConfigurationBuilder;
    }

    AuthenticationConfiguration create(PathConfiguration pathConfiguration) {
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration(pathConfiguration);

        AuthenticationSchemeConfiguration authenticationMethodConfiguration = null;

        if (this.authenticationMethodConfigurationBuilder != null) {
            authenticationMethodConfiguration = this.authenticationMethodConfigurationBuilder.create(authenticationConfiguration);
        }

        authenticationConfiguration.setAuthenticationSchemeConfiguration(authenticationMethodConfiguration);

        return authenticationConfiguration;
    }
}
