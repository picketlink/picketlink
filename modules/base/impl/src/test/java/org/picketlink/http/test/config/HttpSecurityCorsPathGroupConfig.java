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
package org.picketlink.http.test.config;

import org.picketlink.config.http.annotations.AllowedOrigins;
import org.picketlink.config.http.annotations.AllowedRoles;
import org.picketlink.config.http.annotations.Authc;
import org.picketlink.config.http.annotations.Authz;
import org.picketlink.config.http.annotations.Cors;
import org.picketlink.config.http.annotations.ExposedHeaders;
import org.picketlink.config.http.annotations.Expressions;
import org.picketlink.config.http.annotations.Form;
import org.picketlink.config.http.annotations.HttpSecurity;
import org.picketlink.config.http.annotations.MaxAge;
import org.picketlink.config.http.annotations.Path;
import org.picketlink.config.http.annotations.PathGroup;
import org.picketlink.config.http.annotations.SupportAnyHeader;
import org.picketlink.config.http.annotations.SupportedMethods;
import org.picketlink.config.http.annotations.SupportsCredentials;

/**
 * @author Giriraj Sharma
 */
@HttpSecurity
public enum HttpSecurityCorsPathGroupConfig {

    @PathGroup(pathGroupName = "REST Service Group A")
    @Cors
    @AllowedOrigins(origins = { "http://www.example.org:9000", "http://www.example.com:8008" })
    @SupportedMethods(methods = { "POST", "DELETE", "OPTIONS" })
    @SupportAnyHeader
    @ExposedHeaders(headers = { "Authorization" })
    @SupportsCredentials
    @MaxAge(age = 3600)
    @Authc
    @Form(errorPage = "/errorA.html", loginPage = "/loginA.html", restoreOriginalRequest = "no")
    @Authz
    @AllowedRoles(roles = { "Role A", "Role B" })
    @Expressions(expressions = { "#{identity.isLoggedIn()}" })
    @Path(pathGroup = "REST Service Group A", pathName = "/rest/a/*")
    ADMIN_CORS,

}
