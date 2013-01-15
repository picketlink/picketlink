package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * JPA based implementation of PartitionStore
 * 
 * @author Shane Bryzak
 *
 */
public class JPAPartitionStore implements PartitionStore {

    @Override
    public void createPartition(Partition partition) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removePartition(Partition partition) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Realm getRealm(String realmName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tier getTier(String tierId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setup(PartitionStoreConfiguration config, IdentityStoreInvocationContext context) {
        // TODO Auto-generated method stub
        
    }

}
