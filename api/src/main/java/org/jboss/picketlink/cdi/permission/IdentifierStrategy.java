package org.jboss.picketlink.cdi.permission;

import java.io.Serializable;


/**
 * Strategy for generating permission resource identifiers.
 *
 * @author Shane Bryzak
 */
public interface IdentifierStrategy 
{
    boolean canIdentify(Class<?> targetClass);

    /**
     * Returns a String identifier, consisting of the concatenation that includes both the class name of the resource
     * and the identifier value.
     *  
     * @param resource
     * @return
     */
    String getIdentifier(Object resource);
    
    Serializable getIdentifierValue(Object resource);
}
