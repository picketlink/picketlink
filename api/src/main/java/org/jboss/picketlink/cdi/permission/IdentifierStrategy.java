package org.jboss.picketlink.cdi.permission;

import java.io.Serializable;


/**
 * Strategy for generating permission resource identifiers.
 *
 * @author Shane Bryzak
 */
public interface IdentifierStrategy 
{
    /**
     * Returns true if the implementation can identify resources of the specified class
     * 
     * @param resourceClass
     * @return
     */
    boolean canIdentify(Class<?> resourceClass);
    
    /**
     * Returns true if the implementation can load the resource instance for the specified identifier
     * 
     * @param identifier
     * @return
     */
    boolean canLoadResource(String identifier);

    /**
     * Returns a String identifier that can be used to uniquely identify the specified resource
     *  
     * @param resource
     * @return
     */
    String getIdentifier(Object resource);
    
    /**
     * Returns the natural identifier value of the specified resource
     * 
     * @param resource
     * @return
     */
    Serializable getNaturalIdentifier(Object resource);
    
    /**
     * Returns the resource instance for the specified identifier
     * 
     * @param identifier
     * @return
     */
    Object lookupResource(String identifier);
}
