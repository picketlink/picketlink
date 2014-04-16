package org.picketlink.json.jose.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

import static org.picketlink.json.JsonMessages.MESSAGES;

/**
 * @author Pedro Igor
 */
public class HMACSignatureProvider implements SignatureProvider {

    private static HMACSignatureProvider instance;

    private HMACSignatureProvider() {
        // singleton
    }

    static final SignatureProvider instance() {
        if (instance == null) {
            instance = new HMACSignatureProvider();
        }

        return instance;
    }

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

    public boolean verify(byte[] data, Algorithm algorithm, byte[] signature, byte[] key) {
        try {
            return Arrays.equals(sign(data, algorithm, key), signature);
        } catch (Exception e) {
            throw MESSAGES.cryptoSignatureValidationFailed(algorithm, e);
        }
    }

}
