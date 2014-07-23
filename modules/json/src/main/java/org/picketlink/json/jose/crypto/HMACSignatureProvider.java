package org.picketlink.json.jose.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

import static org.picketlink.json.JsonMessages.MESSAGES;

/**
 * The Class HMACSignatureProvider.
 *
 * @author Pedro Igor
 */
public class HMACSignatureProvider implements SignatureProvider {

    /** The instance. */
    private static HMACSignatureProvider instance;

    /**
     * Instantiates a new HMAC signature provider.
     */
    private HMACSignatureProvider() {
        // singleton
    }

    /**
     * Instance.
     *
     * @return the signature provider
     */
    static final SignatureProvider instance() {
        if (instance == null) {
            instance = new HMACSignatureProvider();
        }

        return instance;
    }

    /**
     * @see org.picketlink.json.jose.crypto.SignatureProvider#sign(byte[], org.picketlink.json.jose.crypto.Algorithm, byte[])
     */
    public byte[] sign(byte[] data, Algorithm algorithm, byte[] key) {
        try {
            Mac mac = Mac.getInstance(algorithm.getAlgorithm());
            SecretKeySpec keySpec = new SecretKeySpec(key, mac.getAlgorithm());

            mac.init(keySpec);
            mac.update(data);

            return mac.doFinal();
        } catch (Exception e) {
            throw MESSAGES.cryptoSignatureFailed(algorithm, e);
        }
    }

    /**
     * @see org.picketlink.json.jose.crypto.SignatureProvider#verify(byte[], org.picketlink.json.jose.crypto.Algorithm, byte[], byte[])
     */
    public boolean verify(byte[] data, Algorithm algorithm, byte[] signature, byte[] key) {
        try {
            return Arrays.equals(sign(data, algorithm, key), signature);
        } catch (Exception e) {
            throw MESSAGES.cryptoSignatureValidationFailed(algorithm, e);
        }
    }

}