/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.file.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * @author Pedro Silva
 * 
 */
public abstract class AbstractIdentityTypeEntry<T extends IdentityType> extends AbstractAttributedTypeEntry<T> {

    private static final long serialVersionUID = -142418066761172579L;

    protected AbstractIdentityTypeEntry(String version, T object) {
        super(version, object);
    }

    @Override
    protected T doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        T identityType = super.doPopulateEntry(properties);

        String partitionType = properties.get("partitionType").toString();
        
        Partition partition = null;
        
        if (partitionType.equals(Realm.class.getName())) {
            partition = new Realm(properties.get("partitionName").toString());
        } else if (partitionType.equals(Tier.class.getName())) { 
            partition = new Tier(properties.get("partitionName").toString());
        } else {
            throw new IdentityManagementException("Unsupported partition type [" + partitionType + "].");
        } 

        partition.setId(properties.get("partitionId").toString());

        identityType.setPartition(partition);

        identityType.setCreatedDate((Date) properties.get("createdDate"));
        identityType.setExpirationDate((Date) properties.get("expirationDate"));
        identityType.setEnabled((Boolean) properties.get("enabled"));

        return identityType;
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);
        
        T identityType = getEntry();

        properties.put("partitionName", identityType.getPartition().getName());
        properties.put("partitionId", identityType.getPartition().getId());
        properties.put("partitionType", identityType.getPartition().getClass().getName());
        properties.put("createdDate", identityType.getCreatedDate());
        properties.put("expirationDate", identityType.getExpirationDate());
        properties.put("enabled", identityType.isEnabled());
    }
}
