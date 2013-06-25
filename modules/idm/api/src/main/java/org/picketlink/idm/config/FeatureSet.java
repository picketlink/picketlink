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

    public enum TypeOperation {
        create, read, update, delete
    }

    public enum CredentialOperation {
        create, read, update, delete, validate
    }

    boolean finalized = false;

    private Map<Class<? extends AttributedType>, Set<TypeOperation>> supportedTypes;
    private Map<Class<? extends AttributedType>, Set<TypeOperation>> unsupportedTypes;

    private Map<Class<? extends CredentialStorage>, Set<CredentialOperation>> supportedCredentials;
    private Map<Class<? extends CredentialStorage>, Set<CredentialOperation>> unsupportedCredentials;

    public void supportType(Class<? extends AttributedType> type, TypeOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!supportedTypes.containsKey(type)) {
            supportedTypes.put(type, new HashSet<TypeOperation>());
        }
        for (TypeOperation op : operations) {
            supportedTypes.get(type).add(op);
        }
    }

    public void unsupportType(Class<? extends AttributedType> type, TypeOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!unsupportedTypes.containsKey(type)) {
            unsupportedTypes.put(type, new HashSet<TypeOperation>());
        }
        for (TypeOperation op : operations) {
            unsupportedTypes.get(type).add(op);
        }
    }

    public void supportCredential(Class<? extends CredentialStorage> credential, CredentialOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!supportedCredentials.containsKey(credential)) {
            supportedCredentials.put(credential, new HashSet<CredentialOperation>());
        }
        for (CredentialOperation op : operations) {
            supportedCredentials.get(credential).add(op);
        }
    }

    public void unsupportCredential(Class<? extends CredentialStorage> credential, CredentialOperation... operations) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        if (!unsupportedCredentials.containsKey(credential)) {
            unsupportedCredentials.put(credential, new HashSet<CredentialOperation>());
        }
        for (CredentialOperation op : operations) {
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

    public boolean isTypeOperationSupported(Class<? extends AttributedType> type, TypeOperation operation) {
        boolean supported = false;

        for (Class<? extends AttributedType> cls : supportedTypes.keySet()) {
            if (cls.isAssignableFrom(type) && supportedTypes.get(cls).contains(operation)) {
                supported = true;
                break;
            }
        }

        for (Class<? extends AttributedType> cls : unsupportedTypes.keySet()) {
            if (cls.isAssignableFrom(type) && unsupportedTypes.get(cls).contains(operation)) {
                supported = false;
                break;
            }
        }
        return supported;
    }

    public boolean isCredentialOperationSupported(Class<? extends AttributedType> credentialType, 
            CredentialOperation operation) {
        boolean supported = false;

        for (Class<? extends CredentialStorage> cls : supportedCredentials.keySet()) {
            if (cls.isAssignableFrom(credentialType) && supportedCredentials.get(cls).contains(operation)) {
                supported = true;
                break;
            }
        }

        for (Class<? extends CredentialStorage> cls : unsupportedCredentials.keySet()) {
            if (cls.isAssignableFrom(credentialType) && unsupportedCredentials.get(cls).contains(operation)) {
                supported = false;
                break;
            }
        }
        return supported;
    }

}
