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
package org.picketlink.test.idm.token;

import org.picketlink.idm.credential.AbstractToken;

import java.util.Date;

/**
 * @author Pedro Igor
 */
public class AbstractSimpleToken extends AbstractToken {

    public AbstractSimpleToken(String token) {
        super(token);
    }

    @Override
    public String getSubject() {
        return getClaim(1, "subject");
    }

    public String getIssuer() {
        return getClaim(2, "issuer");
    }

    public String getUserName() {
        return getClaim(3, "userName");
    }

    public Date getExpiration() {
        return new Date(System.currentTimeMillis() + Integer.valueOf(getClaim(4, "expiration")));
    }

    public String getClaim(int position, String name) {
        if (position >= getClaims().length) {
            return null;
        }

        return getClaims()[position].substring(name.length() + 1);
    }

    public String[] getClaims() {
        return getToken().split(";");
    }

}
