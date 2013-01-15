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
     *  A query parameter used to set the key value.
     */
    QueryParameter LOGIN_NAME = new QueryParameter() {};

    /**
     * Returns the login name of this agent.  This value should be unique, as it is used
     * to identify the agent for authentication
     * 
     * @return
     */
    String getLoginName();
}
