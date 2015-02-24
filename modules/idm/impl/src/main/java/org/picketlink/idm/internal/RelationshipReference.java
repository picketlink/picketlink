package org.picketlink.idm.internal;

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Relationship;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Adapter class that encapsulates a target {@link org.picketlink.idm.model.Relationship} instance and provides additional methods
 * to resolve the referenced {@link org.picketlink.idm.model.IdentityType} instances.</p>
 *
 * <p>This class is particularly used when using multiple stores to store different identity and relationship types.
 * In this scenario, the referenced identity type may not be stored in the same store of the relationship, which
 * requires
 * to hold only an identifier-based reference to the referenced type.</p>
 *
 * @author Pedro Igor
 */
public final class RelationshipReference extends AbstractAttributedType implements Relationship {

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
     * <p>Add a reference to a {@link org.picketlink.idm.model.IdentityType}.</p>
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
     * <p>Return the target relationship instance.</p>
     *
     * @return
     */
    public Relationship getRelationship() {
        return relationship;
    }

    public Map<String, String> getIdentityTypeReference() {
        return this.identityTypeReference;
    }
}
