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
package org.picketlink.social.standalone.login;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * An implementation of {@link HttpServletRequestWrapper}
 * @author anil saldhana
 * @since Sep 19, 2012
 */
public class SocialRequestWrapper extends HttpServletRequestWrapper {
    
    protected Principal userPrincipal = null;

    protected HttpServletRequest delegate = null;
    
    public SocialRequestWrapper(HttpServletRequest request) {
        super(request);
        this.delegate = request;
    }
    
    public void setUserPrincipal(Principal principal){
        this.userPrincipal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }
}