package org.picketlink.idm.config;


/**
 * Defines the feature set for an IdentityStore implementation
 *
 * @author Shane Bryzak
 *
 */
public class FeatureSet {

    public enum FeatureGroup {
        agent, user, group, role, relationship, attribute, credential, realm, tier
    }

    public enum FeatureOperation {
        create, read, update, delete, validate
    }

}
