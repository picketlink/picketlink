package org.jboss.picketlink.cdi.permission;

/**
 * Strategy for generating permission resource identifiers.
 *
 * @author Shane Bryzak
 */
public interface IdentifierStrategy 
{
    boolean canIdentify(Class<?> targetClass);

    String getIdentifier(Object target);
}
