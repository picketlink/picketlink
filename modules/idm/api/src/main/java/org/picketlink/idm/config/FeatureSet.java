package org.picketlink.idm.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.AttributedType;


/**
 * Defines the feature set for an IdentityStore implementation
 *
 * @author Shane Bryzak
 *
 */
public final class FeatureSet {

    public enum FeatureOperation {
        create, read, update, delete, validate
    }

    boolean finalized = false;

    private Map<Class<? extends AttributedType>, Set<FeatureOperation>> supportedTypes;
    private Map<Class<? extends AttributedType>, Set<FeatureOperation>> unsupportedTypes;

    private Map<Class<? extends CredentialStorage>, Set<FeatureOperation>> supportedCredentials;
    private Map<Class<? extends CredentialStorage>, Set<FeatureOperation>> unsupportedCredentials;

    public void supportType(Class<? extends AttributedType> type, FeatureOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!supportedTypes.containsKey(type)) {
            supportedTypes.put(type, new HashSet<FeatureOperation>());
        }
        for (FeatureOperation op : operations) {
            supportedTypes.get(type).add(op);
        }
    }

    public void unsupportType(Class<? extends AttributedType> type, FeatureOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!unsupportedTypes.containsKey(type)) {
            unsupportedTypes.put(type, new HashSet<FeatureOperation>());
        }
        for (FeatureOperation op : operations) {
            unsupportedTypes.get(type).add(op);
        }
    }

    public void supportCredential(Class<? extends CredentialStorage> credential, FeatureOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!supportedCredentials.containsKey(credential)) {
            supportedCredentials.put(credential, new HashSet<FeatureOperation>());
        }
        for (FeatureOperation op : operations) {
            supportedCredentials.get(credential).add(op);
        }
    }

    public void unsupportCredential(Class<? extends CredentialStorage> credential, FeatureOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!unsupportedCredentials.containsKey(credential)) {
            unsupportedCredentials.put(credential, new HashSet<FeatureOperation>());
        }
        for (FeatureOperation op : operations) {
            unsupportedCredentials.get(credential).add(op);
        }
    }

    public void finalize() {
        supportedTypes = Collections.unmodifiableMap(supportedTypes);
        unsupportedTypes = Collections.unmodifiableMap(unsupportedTypes);
        supportedCredentials = Collections.unmodifiableMap(supportedCredentials);
        unsupportedCredentials = Collections.unmodifiableMap(unsupportedCredentials);

        finalized = true;
    }

}
