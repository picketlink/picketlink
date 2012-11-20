package org.picketlink.idm.spi;

import org.picketlink.idm.model.Tier;

/**
 * Defines the methods for managing Tiers
 * 
 * @author Shane Bryzak
 *
 */
public interface TierStore {
    /**
     * 
     * @param tier
     */
    void createTier(Tier tier);

    /**
     * 
     * @param tier
     */
    void removeTier(Tier tier);

    /**
     * 
     * @param id
     * @return
     */
    Tier getTier(String id);
}
