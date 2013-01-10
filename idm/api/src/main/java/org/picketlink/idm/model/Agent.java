package org.picketlink.idm.model;

import org.picketlink.idm.query.QueryParameter;

/**
 * Represents an external entity that interacts with the application, such as a user
 * or a third party application
 *  
 * @author Shane Bryzak
 */
public interface Agent extends IdentityType {

    /**
     * A query parameter used to set the id value.
     */
    QueryParameter ID = new QueryParameter() {};

    /**
     * This String prefixes all values returned by the getKey() method.
     */
    String KEY_PREFIX = "AGENT://";

    String getId();
}
