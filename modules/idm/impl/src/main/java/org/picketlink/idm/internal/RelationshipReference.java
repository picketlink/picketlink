package org.picketlink.idm.internal;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA. User: pedroigor Date: 8/1/13 Time: 9:59 PM To change this template use File | Settings |
 * File Templates.
 */
public final class RelationshipReference extends AbstractAttributedType implements Relationship {

    private static final String ID_SEPARATOR = ":";

    private final Relationship relationship;
    private final Map<String, String> identityTypeReference;

    public RelationshipReference(Relationship relationship) {
        this.relationship = relationship;
        this.identityTypeReference = new HashMap<String, String>();
    }

    public void addIdentityTypeReference(String descriptor, String referencedId) {
        this.identityTypeReference.put(descriptor, referencedId);
    }

    public Set<String> getDescriptors() {
        return this.identityTypeReference.keySet();
    }

    public String getPartitionId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[0];
        }

        throw new IdentityManagementException("No Partition id for descriptor [" + descriptor + "].");
    }

    public String getIdentityTypeId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[1];
        }

        throw new IdentityManagementException("No IdentityType id for descriptor [" + descriptor + "].");
    }

    public Relationship getRelationship() {
        return relationship;
    }

    private String[] getReferencedIds(String descriptor) {
        String referencedId = this.identityTypeReference.get(descriptor);

        if (referencedId != null) {
            String[] ids = referencedId.split(":");

            if (ids.length < 2) {
                throw new IdentityManagementException("Wrong format for referenced identitytype id.");
            }

            return ids;
        }

        return null;
    }

    public static String formatId(final IdentityType identityType) {
        return identityType.getPartition().getId() + ID_SEPARATOR + identityType.getId();
    }
}
