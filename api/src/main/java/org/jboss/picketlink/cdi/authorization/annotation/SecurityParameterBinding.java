package org.jboss.picketlink.cdi.authorization.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to an {@link java.lang.annotation.Annotation} to declare it as a security parameter binding; to use business
 * method invocation values as {@link Secures} method arguments.
 * 
 * @author Shane Bryzak
 */
@Documented
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityParameterBinding
{
}
