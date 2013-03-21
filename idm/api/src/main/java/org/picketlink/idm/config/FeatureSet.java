package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.IDMMessages;
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

    /**
     * <p>
     * Adds the given {@link FeatureOperation} for the provided {@link FeatureGroup}.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    public void addFeature(FeatureGroup feature, FeatureOperation operation) throws SecurityConfigurationException {
        checkIfFeatureSetIsLocked();

        getFeatureOperations(feature).add(operation);
    }

    /**
     * <p>
     * Removes the given {@link FeatureOperation} for the provided {@link FeatureGroup} from the features set.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    public void removeFeature(FeatureGroup feature, FeatureOperation operation) throws SecurityConfigurationException {
        checkIfFeatureSetIsLocked();

        getFeatureOperations(feature).remove(operation);

        if (FeatureGroup.relationship.equals(feature)) {
            Set<Entry<Class<? extends Relationship>, Set<FeatureOperation>>> relationShipsEntrySet = this.supportedRelationships
                    .entrySet();

            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : relationShipsEntrySet) {
                getRelationshipOperations(entry.getKey()).remove(operation);
            }
        }
    }

    /**
     * <p>
     * Removes the given {@link FeatureGroup} and all supported {@link FeatureOperation} from the features set.
     * </p>
     *
     * @param feature
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    public void removeFeature(FeatureGroup feature) throws SecurityConfigurationException {
        checkIfFeatureSetIsLocked();

        this.supportedFeatures.remove(feature);

        if (FeatureGroup.relationship.equals(feature)) {
            this.supportedRelationships.clear();
        }
    }

    /**
     * <p>
     * Check if the {@link FeatureGroup} and the given {@link FeatureOperation} are supported.
     * </p>
     *
     * @param feature
     * @param operation
     * @return
     */
    public boolean supports(FeatureGroup feature, FeatureOperation operation) {
        return getFeatureOperations(feature).contains(operation);
    }

    /**
     * <p>
     * Check if the {@link FeatureGroup} is supported.
     * </p>
     *
     * @param feature
     * @param operation
     * @return
     */
    public boolean supports(FeatureGroup feature) {
        return !getFeatureOperations(feature).isEmpty();
    }

    /**
     * <p>
     * Configures the given {@link FeatureOperation} for the provided {@link Relationship} type.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    public void addRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        checkIfFeatureSetIsLocked();
        getRelationshipOperations(relationshipClass).add(operation);
    }

    /**
     * <p>
     * Removes the given {@link FeatureOperation} related to the provided {@link Relationship} type from the feature set.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    public void removeRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        checkIfFeatureSetIsLocked();

        if (!getDefaultRelationshipClasses().contains(relationshipClass) && !this.supportsCustomRelationships) {
            throw new SecurityConfigurationException(
                    "Custom relationships are disabled. You can not add this FeatureOperation to relationship type ["
                            + relationshipClass + "].");
        }

        if (supportedRelationships.containsKey(relationshipClass)) {
            supportedRelationships.get(relationshipClass).remove(operation);
        }
    }

    /**
     * <p>
     * Check if the given {@link FeatureOperation} is supported for the provided {@link Relationship} type.
     * </p>
     *
     * @param feature
     * @param operation
     * @return
     */
    public boolean supportsRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        return getRelationshipOperations(relationshipClass).contains(operation);
    }

    /**
     * <p>
     * Check if the given {@link Relationship} type is supported.
     * </p>
     *
     * @param feature
     * @return
     */
    public boolean supportsRelationship(Class<? extends Relationship> relationshipClass) {
        if (getDefaultRelationshipClasses().contains(relationshipClass)) {
            return this.supportedRelationships.containsKey(relationshipClass);
        } else {
            return this.supportsCustomRelationships && this.supportedRelationships.containsKey(relationshipClass);
        }
    }

    /**
     * <p>
     * Indicates if multi realms are supported or not.
     * </p>
     *
     * @return
     */
    public boolean supportsMultiRealm() {
        return supportsMultiRealm;
    }

    public void setSupportsCustomRelationships(boolean value) {
        checkIfFeatureSetIsLocked();
        this.supportsCustomRelationships = value;
    }

    public void setSupportsMultiRealm(boolean value) {
        checkIfFeatureSetIsLocked();
        this.supportsMultiRealm = value;
    }

    public Map<FeatureGroup, Set<FeatureOperation>> getSupportedFeatures() {
        return Collections.unmodifiableMap(this.supportedFeatures);
    }

    public Map<Class<? extends Relationship>, Set<FeatureOperation>> getSupportedRelationships() {
        return Collections.unmodifiableMap(this.supportedRelationships);
    }

    protected void lock() {
        locked = true;
    }

    /**
     * <p>
     * Adds the given {@link FeatureGroup} values to the provided {@link FeatureSet}.
     * </p>
     *
     * @param featureSet
     * @param groups
     */
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

    /**
     * <p>
     * Adds the given {@link Relationship} types to the provided {@link FeatureSet}. If you do not provide any class only the
     * default ones will be added.
     * </p>
     *
     * @param featureSet
     * @param relationshipClass
     */
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

    /**
     * <p>
     * Removes the given {@link Relationship} types from the provided {@link FeatureSet}.
     * </p>
     *
     * @param featureSet
     * @param relationshipClass
     */
    public static void removeRelationshipSupport(FeatureSet featureSet, Class<? extends Relationship>... relationshipClasses) {
        if (relationshipClasses != null) {
            for (Class<? extends Relationship> relationshipClass : relationshipClasses) {
                featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.create);
                featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.read);
                featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.update);
                featureSet.removeRelationshipFeature(relationshipClass, FeatureOperation.delete);
            }
        }
    }

    protected static List<Class<? extends Relationship>> getDefaultRelationshipClasses() {
        List<Class<? extends Relationship>> classes = new ArrayList<Class<? extends Relationship>>();

        classes.add(Relationship.class);
        classes.add(Grant.class);
        classes.add(GroupMembership.class);
        classes.add(GroupRole.class);

        return classes;
    }

    private static void addBasicOperations(FeatureSet featureSet, FeatureGroup feature) {
        featureSet.addFeature(feature, FeatureOperation.create);
        featureSet.addFeature(feature, FeatureOperation.read);
        featureSet.addFeature(feature, FeatureOperation.update);
        featureSet.addFeature(feature, FeatureOperation.delete);
    }

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

    private void checkIfFeatureSetIsLocked() {
        if (locked) {
            throw IDMMessages.MESSAGES.storeConfigLockedFeatureSet();
        }
    }
}
