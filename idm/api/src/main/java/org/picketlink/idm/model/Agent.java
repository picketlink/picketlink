package org.picketlink.idm.model;


/**
 * Represents an external entity that interacts with the application, such as a user
 * or a third party application
 *  
 * @author Shane Bryzak
 */
public interface Agent extends IdentityType {

    /**
     * This String prefixes all values returned by the getKey() method.
     */
    String KEY_PREFIX = "AGENT://";

    /**
     * Returns the login name of this agent.  This value should be unique, as it is used
     * to identify the agent for authentication
     * 
     * @return
     */
    String getLoginName();
}
