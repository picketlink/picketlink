package org.picketlink.idm.model.annotation;

import org.picketlink.idm.model.IdentityType;

/**
 * Used to annotate a custom partition type and define the identity types that it supports
 * 
 * @author Shane Bryzak
 *
 */
public @interface IdentityPartition {
    Class<? extends IdentityType>[] supportedTypes() default {};
    Class<? extends IdentityType>[] unsupportedTypes() default {};
}
