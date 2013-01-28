/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.common.exceptions.fed;

import java.security.GeneralSecurityException;

/**
 * Security Exception indicating expiration of SAML2 assertion
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 12, 2008
 */
public class AssertionExpiredException extends GeneralSecurityException {
    private static final long serialVersionUID = 1L;

    protected String id;

    public AssertionExpiredException() {
    }

    public AssertionExpiredException(String message, Throwable cause) {
    }

    public AssertionExpiredException(String msg) {
        super(msg);
    }

    public AssertionExpiredException(Throwable cause) {
        super(cause);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}