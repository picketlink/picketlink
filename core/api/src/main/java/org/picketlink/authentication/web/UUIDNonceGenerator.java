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
package org.picketlink.authentication.web;

import java.util.UUID;

import org.picketlink.common.util.Base64;

/**
 * An instance of {@link NonceGenerator} that uses {@link UUID} level 4
 *
 * @author anil saldhana
 */
public class UUIDNonceGenerator {
    
    public String get() {
        StringBuilder sb = new StringBuilder(System.currentTimeMillis() + ":");
        sb.append(UUID.randomUUID().toString());
        return Base64.encodeBytes(sb.toString().getBytes());
    }

    public boolean hasExpired(String nonceValue, long maxValue) {
        nonceValue = new String(Base64.decode(nonceValue));
        int colonIndex = nonceValue.indexOf(":");
        if (colonIndex < 0)
            return true;
        String timeValue = nonceValue.substring(0, colonIndex);
        long parsedTimeValue = Long.parseLong(timeValue);
        long ms = System.currentTimeMillis() - parsedTimeValue;
        if (ms > maxValue) {
            return true;
        }
        return false;
    }
}