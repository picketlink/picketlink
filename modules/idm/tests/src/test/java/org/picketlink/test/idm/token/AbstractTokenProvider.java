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

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

import java.util.Date;
import java.util.UUID;

/**
 * @author Pedro Igor
 */
public abstract class AbstractTokenProvider<T extends Token> implements Token.Provider<T> {

    private final PartitionManager partitionManager;
    private final Date expirationTime;

    public AbstractTokenProvider(PartitionManager partitionManager, Date expirationTime) {
        this.partitionManager = partitionManager;
        this.expirationTime = expirationTime;
    }

    @Override
    public T issue(Account account) {
        IdentityManager identityManager = getIdentityManager(account);

        T token = createToken(getTokenType(), (User) account);

        identityManager.updateCredential(account, token, null, this.expirationTime);

        return token;
    }

    private IdentityManager getIdentityManager(Account account) {
        return this.partitionManager.createIdentityManager(account.getPartition());
    }

    @Override
    public T renew(Account account, T renewToken) {
        return issue(account);
    }

    @Override
    public void invalidate(Account account) {
        getIdentityManager(account).removeCredential(account, getCredentialStorageType());
    }

    protected abstract Class<? extends TokenCredentialStorage> getCredentialStorageType();

    public static <T extends Token> T createToken(Class<T> tokenType, User user) {
        if (user.getPartition() == null) {
            throw new IllegalArgumentException("User does not have a partition.");
        }

        StringBuilder builder = new StringBuilder();

        builder
            .append("id=").append(UUID.randomUUID().toString())
            .append(";")
            .append("subject=").append(user.getId())
            .append(";")
            .append("issuer=").append(user.getPartition().getName())
            .append(";")
            .append("userName=").append(user.getLoginName())
            .append(";")
            .append("expiration=").append(1000)
            .append(";");

        return (T) Token.Builder.create(tokenType.getName(), builder.toString());
    }
}
