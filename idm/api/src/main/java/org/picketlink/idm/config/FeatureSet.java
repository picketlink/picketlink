package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        agent, user, group, role, relationship, attribute, credential, realm, tier
    }

    public enum FeatureOperation {
        create, read, update, delete, validate
    }

    /**
     * Metadata reflecting which features are supported by this identity store
     */
    private final Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures = new HashMap<FeatureGroup, Set<FeatureOperation>>();

    private final Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships = new HashMap<Class<? extends Relationship>, Set<FeatureOperation>>();

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
            throw new SecurityConfigurationException(
                    "Feature set has already been locked, no additional features may be added.");
        }
        getFeatureOperations(feature).add(operation);
    }

    public void removeFeature(FeatureGroup feature, FeatureOperation operation) {
        getFeatureOperations(feature).remove(operation);

        if (FeatureGroup.relationship.equals(feature)) {
            Set<Entry<Class<? extends Relationship>, Set<FeatureOperation>>> relationShipsEntrySet = this.supportedRelationships
                    .entrySet();

            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : relationShipsEntrySet) {
                getRelationshipOperations(entry.getKey()).remove(operation);
            }
        }
    }

    public void removeFeature(FeatureGroup feature) {
        this.supportedFeatures.remove(feature);

        if (FeatureGroup.relationship.equals(feature)) {
            this.supportedRelationships.clear();
        }
    }

    public boolean supports(FeatureGroup feature, FeatureOperation operation) {
        return getFeatureOperations(feature).contains(operation);
    }

    public boolean supports(FeatureGroup feature) {
        return !getFeatureOperations(feature).isEmpty();
    }

    public void addRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        if (locked) {
            throw new SecurityConfigurationException(
                    "Feature set has already been locked, no additional features may be added.");
        }
        
        getRelationshipOperations(relationshipClass).add(operation);
    }

    public void removeRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        if (locked) {
            throw new SecurityConfigurationException(
                    "Feature set has already been locked, no additional features may be added.");
        }
        
        if (!getDefaultRelationshipClasses().contains(relationshipClass) && !this.supportsCustomRelationships) {
            throw new SecurityConfigurationException(
                    "Custom relationships are disabled. You can not add this FeatureOperation to relationship type [" + relationshipClass + "].");
        }
        
        if (supportedRelationships.containsKey(relationshipClass)) {
            supportedRelationships.get(relationshipClass).remove(operation);
        }
    }

    public boolean supportsRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        return getRelationshipOperations(relationshipClass).contains(operation);
    }

    public boolean supportsRelationship(Class<? extends Relationship> relationshipClass) {
        if (getDefaultRelationshipClasses().contains(relationshipClass)) {
            return this.supportedRelationships.containsKey(relationshipClass);            
        } else {
            return this.supportsCustomRelationships && this.supportedRelationships.containsKey(relationshipClass);
        }
    }

    public boolean supportsMultiRealm() {
        return supportsMultiRealm;
    }

    public void setSupportsCustomRelationships(boolean value) {
        if (locked && value) {
            throw new SecurityConfigurationException(
                    "Feature set has already been locked, no additional features may be added.");
        }
        this.supportsCustomRelationships = value;
    }

    public void setSupportsMultiRealm(boolean value) {
        if (locked && value) {
            throw new SecurityConfigurationException(
                    "Feature set has already been locked, no additional features may be added.");
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
                    addBasicOperations(featureSet, feature);
                    break;
                case user:
                    addBasicOperations(featureSet, feature);
                    break;
                case group:
                    addBasicOperations(featureSet, feature);
                    break;
                case role:
                    addBasicOperations(featureSet, feature);
                    break;
                case relationship:
                    addRelationshipSupport(featureSet, null);
                    addBasicOperations(featureSet, feature);
                    break;
                case attribute:
                    addBasicOperations(featureSet, feature);
                    break;
                case realm:
                    addBasicOperations(featureSet, feature);
                    break;
                case tier:
                    addBasicOperations(featureSet, feature);
                    break;
                case credential:
                    featureSet.addFeature(feature, FeatureOperation.update);
                    featureSet.addFeature(feature, FeatureOperation.validate);
                    break;
            }
        }
    }

    private static void addBasicOperations(FeatureSet featureSet, FeatureGroup feature) {
        featureSet.addFeature(feature, FeatureOperation.create);
        featureSet.addFeature(feature, FeatureOperation.read);
        featureSet.addFeature(feature, FeatureOperation.update);
        featureSet.addFeature(feature, FeatureOperation.delete);
    }

    public static void addRelationshipSupport(FeatureSet featureSet, Class<? extends Relationship>... relationshipClass) {
        List<Class<? extends Relationship>> classes;

        if (relationshipClass != null && relationshipClass.length > 0) {
            classes = Arrays.<Class<? extends Relationship>> asList(relationshipClass);
        } else {
            classes = getDefaultRelationshipClasses();
        }

        for (Class<? extends Relationship> cls : classes) {
            featureSet.addRelationshipFeature(cls, FeatureOperation.create);
            featureSet.addRelationshipFeature(cls, FeatureOperation.read);
            featureSet.addRelationshipFeature(cls, FeatureOperation.update);
            featureSet.addRelationshipFeature(cls, FeatureOperation.delete);
        }
    }

    public static void removeRelationshipSupport(FeatureSet featureSet, Class<? extends Relationship>... relationshipClasses) {
        for (Class<? extends Relationship> relationshipClass : relationshipClasses) {
            featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.create);
            featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.read);
            featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.update);
            featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.delete);
        }
    }

    private static List<Class<? extends Relationship>> getDefaultRelationshipClasses() {
        List<Class<? extends Relationship>> classes = new ArrayList<Class<? extends Relationship>>();

        classes.add(Relationship.class);
        classes.add(Grant.class);
        classes.add(GroupMembership.class);
        classes.add(GroupRole.class);

        return classes;
    }
}
