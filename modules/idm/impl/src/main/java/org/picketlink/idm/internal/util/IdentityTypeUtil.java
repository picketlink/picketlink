/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.idm.internal.util;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;

import static org.picketlink.common.reflection.Reflections.classForName;

/**
 * @author pedroigor
 */
public class IdentityTypeUtil {

    private static final String ID_SEPARATOR = ":";

    public static IdentityType resolveIdentityType(String descriptor, Object parent, PartitionManager partitionManager) {
        String type = IdentityTypeUtil.getIdentityType(descriptor);
        String partitionId = IdentityTypeUtil.getPartitionId(descriptor);
        String identityTypeId = IdentityTypeUtil.getIdentityTypeId(descriptor);

        Partition partition = partitionManager.lookupById(Partition.class, partitionId);

        if (partition == null) {
            throw new IdentityManagementException("No partition [" + partitionId + "] found for " +
                    "referenced IdentityType [" + identityTypeId + "].");
        }

        Class<? extends IdentityType> identityTypeClass;

        try {
            identityTypeClass = classForName(type, parent.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IdentityManagementException("Could not instantiate referenced identity type [" + type + "].", e);
        }

        IdentityManager identityManager = partitionManager.createIdentityManager(partition);
        IdentityType identityType = identityManager.lookupById(identityTypeClass, identityTypeId);

        if (identityType == null) {
            throw new IdentityManagementException("Referenced IdentityType [" + identityTypeId + "] from " +
                    "" +
                    "relationship " +
                    "[" + parent.getClass() + "] does not exists in any store.");
        }

        return identityType;
    }

    /**
     * <p>Return a formatted string representing the reference to the given {@link org.picketlink.idm.model.IdentityType}.</p>
     *
     * @param identityType
     *
     * @return
     */
    public static String formatId(final IdentityType identityType) {
        return identityType.getClass().getName() + ID_SEPARATOR + identityType.getPartition().getId() + ID_SEPARATOR + identityType.getId();
    }

    /**
     * <p>Return the identifier of the partition where the identity type is stored.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public static String getPartitionId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[1];
        }

        throw new IdentityManagementException("No Partition id for descriptor [" + descriptor + "].");
    }

    /**
     * <p>Return the type given a descriptor.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public static String getIdentityType(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[0];
        }

        throw new IdentityManagementException("No type defined for descriptor [" + descriptor + "].");
    }

    /**
     * <p>Return the identifier of the identity type referenced by the descriptor.</p>
     *
     * @param descriptor
     *
     * @return
     */
    public static String getIdentityTypeId(String descriptor) {
        String[] referencedIds = getReferencedIds(descriptor);

        if (referencedIds != null) {
            return referencedIds[2];
        }

        throw new IdentityManagementException("No IdentityType id for descriptor [" + descriptor + "].");
    }

    public static String[] getReferencedIds(String referencedId) {
        if (referencedId != null) {
            String[] ids = referencedId.split(ID_SEPARATOR);

            if (ids.length < 2) {
                throw new IdentityManagementException("Wrong format for referenced identitytype id.");
            }

            return ids;
        }

        return null;
    }

}
