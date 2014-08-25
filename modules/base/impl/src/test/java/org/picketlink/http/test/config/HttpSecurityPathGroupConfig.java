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

import org.picketlink.config.http.annotations.AllowedRoles;
import org.picketlink.config.http.annotations.Authc;
import org.picketlink.config.http.annotations.Authz;
import org.picketlink.config.http.annotations.Form;
import org.picketlink.config.http.annotations.HttpSecurity;
import org.picketlink.config.http.annotations.Path;
import org.picketlink.config.http.annotations.PathGroup;

/**
 * @author Giriraj Sharma
 */
@HttpSecurity
public enum HttpSecurityPathGroupConfig {

    @PathGroup(pathGroupName = "REST Service Group A")
    @Authc
    @Form(errorPage = "/errorA.html", loginPage = "/loginA.html", restoreOriginalRequest = "no")
    @Authz
    @AllowedRoles(roles = { "Role A" })
    @Path(pathGroup = "REST Service Group A", pathName = "/rest/a/*")
    CLIENT_GROUP_A,

    @PathGroup(pathGroupName = "REST Service Group B")
    @Authc
    @Form(errorPage = "/errorB.html", loginPage = "/loginB.html", restoreOriginalRequest = "no")
    @Authz
    @AllowedRoles(roles = { "Role B" })
    @Path(pathGroup = "REST Service Group B", pathName = "/rest/b/*")
    CLIENT_GROUP_B;

}
