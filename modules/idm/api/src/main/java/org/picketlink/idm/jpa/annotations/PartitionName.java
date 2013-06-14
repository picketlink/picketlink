package org.picketlink.idm.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks the partition name property of a partition entity.  This property should return
 * the name of the partition, unique for the partition class.
 *
 * @author Shane Bryzak
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface PartitionName {

}
