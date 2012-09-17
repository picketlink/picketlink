package org.jboss.picketlink.cdi.permission.spi;

import java.io.Serializable;
import java.util.Set;


/**
 * Handles the generation of permission resource identifiers, and is responsible for the
 * marshaling / unmarshaling of permissions 
 *
 * @author Shane Bryzak
 */
public interface PermissionHandler 
{
    /**
     * Returns true if the implementation can handle resources of the specified class
     * 
     * @param resourceClass
     * @return
     */
    boolean canHandle(Class<?> resourceClass);
    
    /**
     * Returns true if the implementation can load the resource instance for the specified identifier
     * 
     * @param identifier
     * @return
     */
    boolean canLoadResource(String identifier);

    /**
     * Returns a String identifier value that can be used to uniquely identify the specified resource
     *  
     * @param resource
     * @return
     */
    String getGeneratedIdentifier(Object resource);
    
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
    
    /**
     * Returns a set containing the available permissions for a particular resource.  If there are no hard coded
     * permissions defined (i.e. any permission is allowed) then this method will return an empty set.
     * 
     * @param resourceClass
     * @return
     */
    Set<String> listAvailablePermissions(Class<?> resourceClass);
}
