package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Relationship;

/**
 * Defines the feature set for an IdentityStore implementation
 * 
 * @author Shane Bryzak
 *
 */
public class FeatureSet {

    public enum FeatureGroup {
        agent,
        user,
        group,
        role,
        relationship,
        attribute,
        credential,
        realm,
        tier
    }

    public enum FeatureOperation {
        create,
        read,
        update,
        delete,
        validate
    }

    /**
     * Metadata reflecting which features are supported by this identity store
     */
    private final Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures = new HashMap<FeatureGroup, Set<FeatureOperation>>();

    private final Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships = 
            new HashMap<Class<? extends Relationship>, Set<FeatureOperation>>();

    private boolean supportsCustomRelationships = false;

    private boolean supportsMultiRealm = false;

    private boolean locked = false;

    private Set<FeatureOperation> getFeatureOperations(FeatureGroup group) {
        if (!supportedFeatures.containsKey(group)) {
            supportedFeatures.put(group, new HashSet<FeatureOperation>());
        }
        return supportedFeatures.get(group);
    }

    private Set<FeatureOperation> getRelationshipOperations(Class<? extends Relationship> relationshipClass) {
        if (!supportedRelationships.containsKey(relationshipClass)) {
            supportedRelationships.put(relationshipClass, new HashSet<FeatureOperation>());
        }
        return supportedRelationships.get(relationshipClass);
    }

    public void addFeature(FeatureGroup feature, FeatureOperation operation) {
        if (locked) {
            throw new SecurityConfigurationException("Feature set has already been locked, no additional features may be added.");
        }
        getFeatureOperations(feature).add(operation);
    }

    public void removeFeature(FeatureGroup feature, FeatureOperation operation) {
        getFeatureOperations(feature).remove(operation);
    }
    
    public void removeFeature(FeatureGroup feature) {
        this.supportedFeatures.remove(feature);
    }

    public boolean supports(FeatureGroup feature, FeatureOperation operation) {
        return getFeatureOperations(feature).contains(operation);
    }

    public void addRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        if (locked) {
            throw new SecurityConfigurationException("Feature set has already been locked, no additional features may be added.");
        }
        getRelationshipOperations(relationshipClass).add(operation);
    }

    public boolean supportsRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        return getRelationshipOperations(relationshipClass).contains(operation);
    }

    public boolean supportsMultiRealm() {
        return supportsMultiRealm;
    }

    public void setSupportsCustomRelationships(boolean value) {
        if (locked && value) {
            throw new SecurityConfigurationException("Feature set has already been locked, no additional features may be added.");
        }
        this.supportsCustomRelationships = value;
    }

    public void setSupportsMultiRealm(boolean value) {
        if (locked && value) {
            throw new SecurityConfigurationException("Feature set has already been locked, no additional features may be added.");
        }
        this.supportsMultiRealm = value;
    }

    public void lock() {
        locked = true;
    }

    public static void addFeatureSupport(FeatureSet featureSet, FeatureGroup... groups) {
        FeatureGroup[] features = (groups != null && groups.length > 0) ? groups : FeatureGroup.values();

        for (FeatureGroup feature : features) {
            switch (feature) {
                case agent:
                case user:
                case group:
                case role:
                case relationship:
                case attribute:
                case realm:
                case tier:
                    featureSet.addFeature(feature, FeatureOperation.create);
                    featureSet.addFeature(feature, FeatureOperation.read);
                    featureSet.addFeature(feature, FeatureOperation.update);
                    featureSet.addFeature(feature, FeatureOperation.delete);
                    break;
                case credential:
                    featureSet.addFeature(feature, FeatureOperation.update);
                    featureSet.addFeature(feature, FeatureOperation.validate);
                    break;
            }
        }
    }

    public static void addRelationshipSupport(FeatureSet featureSet, Class<? extends Relationship>... relationshipClass) {
        List<Class<? extends Relationship>> classes;

        if (relationshipClass != null && relationshipClass.length > 0)  {
            classes = Arrays.<Class<? extends Relationship>>asList(relationshipClass); 
        } else {
            classes = new ArrayList<Class<? extends Relationship>>();
            classes.add(Grant.class);
            classes.add(GroupMembership.class);
            classes.add(GroupRole.class);
        }

        for (Class<? extends Relationship> cls : classes) {
            featureSet.addRelationshipFeature(cls, FeatureOperation.create);
            featureSet.addRelationshipFeature(cls, FeatureOperation.read);
            featureSet.addRelationshipFeature(cls, FeatureOperation.update);
            featureSet.addRelationshipFeature(cls, FeatureOperation.delete);
        }
    }
}
