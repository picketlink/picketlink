package org.picketlink.idm.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private Map<Class<? extends AttributedType>, Set<TypeOperation>> supportedTypes =
            new HashMap<Class<? extends AttributedType>, Set<TypeOperation>>();
    private Map<Class<? extends AttributedType>, Set<TypeOperation>> unsupportedTypes =
            new HashMap<Class<? extends AttributedType>, Set<TypeOperation>>();

    private Set<CredentialOperation> credentialOperations = new HashSet<CredentialOperation>();

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

    public void supportCredentialOperation(CredentialOperation operation) {
        if (finalized) {
            throw new IllegalStateException("FeatureSet is finalized and can no longer be updated");
        }

        credentialOperations.add(operation);
    }

    public void finalize() {
        supportedTypes = Collections.unmodifiableMap(supportedTypes);
        unsupportedTypes = Collections.unmodifiableMap(unsupportedTypes);
        credentialOperations = Collections.unmodifiableSet(credentialOperations);

        finalized = true;
    }

    public int isTypeOperationSupported(Class<? extends AttributedType> type, TypeOperation operation) {
        int score = -1;

        for (Class<? extends AttributedType> cls : supportedTypes.keySet()) {
            int clsScore = calcScore(type, cls);
            if (clsScore > score && supportedTypes.get(cls).contains(operation)) {
                score = clsScore;
            }
        }

        for (Class<? extends AttributedType> cls : unsupportedTypes.keySet()) {
            if (cls.isAssignableFrom(type) && unsupportedTypes.get(cls).contains(operation)) {
                score = -1;
                break;
            }
        }
        return score;
    }

    public boolean isCredentialOperationSupported(CredentialOperation operation) {
        return credentialOperations.contains(operation);
    }

    private int calcScore(Class<?> type, Class<?> targetClass) {
        if (type.equals(targetClass)) {
            return 0;
        } else if (targetClass.isAssignableFrom(type)) {
            int score = 0;

            Class<?> cls = type.getSuperclass();
            while (!cls.equals(Object.class)) {
                if (targetClass.isAssignableFrom(cls)) {
                    score++;
                } else {
                    break;
                }
            }
            return score;
        } else {
            return -1;
        }
    }
}
