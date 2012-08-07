package org.jboss.picketlink.cdi.permission.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes an entity bean (i.e. a class annotated with @Entity) as being a storage container
 * for object permissions.  If the value member is set, then the annotated entity will be used
 * to lookup object permissions for objects of that class only, otherwise if it is not set the
 * entity will be used to store general object permissions (only one entity may be used for
 * general permissions, if more than one entity is defined then a deployment exception will be
 * thrown).
 * 
 * @author Shane Bryzak
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface ACLStore
{
    Class<?> value() default GENERAL.class;
    
    // Dummy class to enable the entity bean for general storage of ACL permissions
    static final class GENERAL 
    { }
}
