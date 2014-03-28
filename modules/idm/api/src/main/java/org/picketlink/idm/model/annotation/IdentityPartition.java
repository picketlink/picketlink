package org.picketlink.idm.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.picketlink.idm.model.IdentityType;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to annotate a custom partition type and define the identity types that it supports and
 * doesn't support.  Any IdentityType class and its subclasses defined by the supportedTypes
 * member will be supported by the partition, unless explicitly defined by the unsupportedTypes member.
 *
 * For example, in the class hierarchy A -> B -> C:
 *
 * 1) If supportedTypes = A and unsupportedTypes = C, then both A and B are supported and C is not.
 * 2) If supportedTypes = A and unsupportedTypes = B, then A is supported and B and C are not.
 *
 * It is invalid to specify an unsupportedType higher in the class hierarchy than a supportedType.
 * Unsupported types must always be "trimmed" off the branches of the hierarchy.
 *
 *
 * @author Shane Bryzak
 *
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface IdentityPartition {
    Class<? extends IdentityType>[] supportedTypes() default {IdentityType.class};
    Class<? extends IdentityType>[] unsupportedTypes() default {};
}
