package org.picketlink.idm.model;

import java.io.Serializable;

/**
 * An abstract container representing a grouping of identity objects.  Realm and Tier are examples
 * of Partitions.
 * 
 * @author Shane Bryzak
 */
public interface Partition extends Serializable {
    
    void setName(String name);
    
    String getName();
    
    String getKey();

    String getId();
    
    void setId(String id);
}
