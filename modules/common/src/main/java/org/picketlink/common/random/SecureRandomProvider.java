package org.picketlink.common.random;

import java.security.SecureRandom;

/**
 * Provides initialized and seeded instance of SecureRandom
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface SecureRandomProvider {

    /**
     * @return initialized and seeded instance of SecureRandom
     */
     SecureRandom getSecureRandom();

}
