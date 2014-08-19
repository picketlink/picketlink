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
 * <p>Configures HTTP CLIENT-CERT Authentication for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class X509AuthenticationConfigurationBuilder extends AuthenticationMethodConfigurationBuilder {

    private String subjectRegex;

    X509AuthenticationConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    /**
     * <p>Specifies a Java Regex that will be used to extract the subject's identifier from the certificate.</p>
     *
     * @param expression
     * @return
     */
    public X509AuthenticationConfigurationBuilder subjectRegex(String expression) {
        this.subjectRegex = expression;
        return this;
    }

    @Override
    AuthenticationSchemeConfiguration create(AuthenticationConfiguration authenticationConfiguration) {
        return new X509AuthenticationConfiguration(this.subjectRegex, authenticationConfiguration);
    }
}
