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

package org.picketlink.authentication.web.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.credential.Digest;

/**
 * @author Pedro Silva
 */
public class NonceCache extends TimerTask {

    private UUIDNonceGenerator nonceGenerator = new UUIDNonceGenerator();

    private long nonceMaxValid = 3 * 60 * 1000;

    private Map<String, List<String>> nonceCache = new HashMap<String, List<String>>();

    @Override
    public void run() {
        if (nonceCache.isEmpty()) {
            return;
        }

        Set<Entry<String, List<String>>> cacheEntries = new HashMap<String, List<String>>(this.nonceCache).entrySet();

        for (Entry<String, List<String>> entry : cacheEntries) {
            List<String> nonces = entry.getValue();

            for (String nonce : new ArrayList<String>(nonces)) {
                if (hasExpired(nonce, this.nonceMaxValid)) {
                    nonces.remove(nonce);
                }
            }

            if (nonces.isEmpty()) {
                this.nonceCache.remove(entry.getKey());
            }
        }
    }

    public String generateAndCacheNonce(HttpServletRequest request) {
        String newNonce = this.nonceGenerator.get();

        HttpSession session = request.getSession();

        List<String> storedNonces = this.nonceCache.get(session.getId());

        if (storedNonces == null) {
            storedNonces = new ArrayList<String>();
            this.nonceCache.put(session.getId(), storedNonces);
        }

        storedNonces.add(newNonce);

        return newNonce;
    }

    public boolean hasValidNonce(Digest digest, HttpServletRequest request) {
        String nonce = digest.getNonce();

        List<String> storedNonces = this.nonceCache.get(request.getSession().getId());

        if (storedNonces == null || !storedNonces.contains(nonce) || hasExpired(nonce, this.nonceMaxValid)) {
            return false;
        }

        return true;
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

    public long getNonceMaxValid() {
        return this.nonceMaxValid;
    }
}
