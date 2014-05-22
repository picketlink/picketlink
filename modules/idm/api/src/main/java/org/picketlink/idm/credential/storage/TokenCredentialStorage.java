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
package org.picketlink.idm.credential.storage;

import org.picketlink.idm.credential.storage.annotations.Stored;

/**
 * <p>{@link org.picketlink.idm.credential.storage.CredentialStorage} used to store token-based credentials. Token value
 * is always represented by a string.</p>
 *
 * <p>This default implementation stores the only the <code>type</code> and <code>value</code> of a token.</p>
 *
 * <p>Classes can provide additional state by extending this class and use the {@link org.picketlink.idm.credential.Token.Provider#getTokenStorage(org.picketlink.idm.model.Account, org.picketlink.idm.credential.Token)}
 * method to provide the additional state.</p>
 *
 * @author Pedro Igor
 * @see org.picketlink.idm.credential.handler.TokenCredentialHandler
 * @see org.picketlink.idm.credential.Token.Provider
 */
public class TokenCredentialStorage extends AbstractCredentialStorage {

    private String type;
    private String value;

    @Stored
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Stored
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
