/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.idm.credential.random;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.picketlink.idm.IdentityManagementException;
import static org.picketlink.idm.IDMLogger.*;

/**
 * <p>SecureRandomProvider initialized with random seed. It's also periodically reseeding itself, so the seed can't be guessed.</p>
 *
 * <p>It's good for production environment, however may not be so good for testing, as initialization of SecureRandom could be slow.
 * The initialization is performed in separate thread, so it could be started even before first random number is obtained and it's not
 * blocking caller</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AutoReseedSecureRandomProvider implements SecureRandomProvider {

    private AtomicReference<SecureRandom> reference = new AtomicReference<SecureRandom>();

    private volatile boolean started = false;
    private final CountDownLatch initializationLatch = new CountDownLatch(1);

    // 5 minutes by default
    private static final int DEFAULT_RESEED_INTERVAL = 1000 * 60 *5;

    @Override
    public SecureRandom getSecureRandom() {
        if (!this.started) {
            throw new IllegalStateException("AutoReseedSecureRandomProvider needs to be started before it's used");
        }

        try {
            initializationLatch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        return reference.get();
    }

    public void start() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                LOGGER.startSecureRandomInitialization();
                SecureRandom secureRandom = initSecureRandom();
                reference.set(secureRandom);
                LOGGER.secureRandomInitialized();

                initializationLatch.countDown();

                // Re-init provider periodically
                while (true) {
                    try {
                        Thread.sleep(DEFAULT_RESEED_INTERVAL);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    secureRandom = initSecureRandom();
                    reference.set(secureRandom);
                    LOGGER.secureRandomReinitialized();
                }
            }

        }, AutoReseedSecureRandomProvider.class.getSimpleName() + " worker");

        // Make a daemon so that the VM can be shut down before it completes
        t.setDaemon(true);
        t.start();

        this.started = true;
    }

    protected SecureRandom initSecureRandom() {
        try {
            SecureRandom pseudoRandom = SecureRandom.getInstance(DEFAULT_SALT_ALGORITHM);
            // Force seed initialization
            pseudoRandom.nextLong();
            return pseudoRandom;
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityManagementException("Error getting SecureRandom instance: " + DEFAULT_SALT_ALGORITHM, e);
        }
    }


}
