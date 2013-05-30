package org.picketlink.idm.config;


/**
 * Defines the feature set for an IdentityStore implementation
 *
 * @author Shane Bryzak
 *
 */
public final class FeatureSet {

    public enum FeatureGroup {
        identity_type, relationship, attribute, credential, realm, tier
    }

    public enum FeatureOperation {
        create, read, update, delete, validate
    }

}
