package org.picketlink.common.random;

import java.security.SecureRandom;
import java.util.Random;

/**
 * <p>A default implementation of {@link SecureRandomProvider}.</p>
 *
 * <p>This class can be configured with both algorithm and key length. If the key length is <= 0, {@link SecureRandom} instances
 * will be created without a previously generated random seed.</p>
 *
 * @author pedroigor
 */
public class DefaultSecureRandomProvider implements SecureRandomProvider {

    public static final String DEFAULT_SALT_ALGORITHM = "SHA1PRNG";

    private final String algorithm;
    private final int keyLength;

    public DefaultSecureRandomProvider() {
        this(DEFAULT_SALT_ALGORITHM, 0);
    }

    public DefaultSecureRandomProvider(String algorithm, int keyLength) {
        this.algorithm = algorithm;
        this.keyLength = keyLength;
    }

    @Override
    public SecureRandom getSecureRandom() {
        SecureRandom secureRandom;

        try {
            secureRandom = SecureRandom.getInstance(this.algorithm);

            if (this.keyLength > 0) {
                secureRandom.setSeed(secureRandom.generateSeed(this.keyLength));
            } else {
                secureRandom.setSeed(new Random().nextLong());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error getting SecureRandom instance: " + this.algorithm, e);
        }

        return secureRandom;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public int getKeyLength() {
        return this.keyLength;
    }
}
