package org.picketlink.idm.internal;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Adapter class that encapsulates a target {@link Relationship} instance and provides additional methods
 * to resolve the referenced {@link IdentityType} instances.</p>
 *
 * <p>This class is particularly used when using multiple stores to store different identity and relationship types.
 * In this scenario, the referenced identity type may not be stored in the same store of the relationship, which
 * requires
 * to hold only an identifier-based reference to the referenced type.</p>
 *
 * @author Pedro Igor
 */
public final class RelationshipReference extends AbstractAttributedType implements Relationship {

    private static final String ID_SEPARATOR = ":";

    private final Relationship relationship;
    private final Map<String, String> identityTypeReference;

    public RelationshipReference(Relationship relationship) {
        this.relationship = relationship;
        this.identityTypeReference = new HashMap<String, String>();
    }

    @Override
    public String getId() {
        return getRelationship().getId();
    }

    /**
     * <p>Add a reference to a {@link IdentityType}.</p>
     *
     * @param descriptor The descriptor for the identity type. The descriptor usually matches the property name on the
     * target
     * relationship instance used to store the identity type instance.
     * @param referencedId The identifier of the identity type.
     */
    public void addIdentityTypeReference(String descriptor, String referencedId) {
        this.identityTypeReference.put(descriptor, referencedId);
    }

    /**
     * <p>Returns a {@link Set} of strings representing all registered identity type references.</p>
     *
     * <p>Descriptors have a format: <code>typeName:partitionId:identityTypeId</code>.</p>
     *
     * @return
     */
    public Set<String> getDescriptors() {
        return this.identityTypeReference.keySet();
    }

    /**
     * <p>Return the type given a descriptor.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public String getIdentityType(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[0];
        }

        throw new IdentityManagementException("No type defined for descriptor [" + descriptor + "].");
    }

    /**
     * <p>Return the identifier of the partition where the identity type is stored.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public String getPartitionId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[1];
        }

        throw new IdentityManagementException("No Partition id for descriptor [" + descriptor + "].");
    }

    /**
     * <p>Return the identifier of the identity type referenced by the descriptor.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public String getIdentityTypeId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[2];
        }

        throw new IdentityManagementException("No IdentityType id for descriptor [" + descriptor + "].");
    }

    /**
     * <p>Return the target relationship instance.</p>
     *
     * @return
     */
    public Relationship getRelationship() {
        return relationship;
    }

    private String[] getReferencedIds(String descriptor) {
        String referencedId = this.identityTypeReference.get(descriptor);

        if (referencedId != null) {
            String[] ids = referencedId.split(ID_SEPARATOR);

            if (ids.length < 2) {
                throw new IdentityManagementException("Wrong format for referenced identitytype id.");
            }

            return ids;
        }

        return null;
    }

    /**
     * <p>Return a formatted string representing the reference to the given {@link IdentityType}.</p>
     *
     * @param identityType
     *
     * @return
     */
    public static String formatId(final IdentityType identityType) {
        return identityType.getClass().getName() + ID_SEPARATOR + identityType.getPartition().getId() + ID_SEPARATOR + identityType.getId();
    }
}
