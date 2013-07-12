package org.picketlink.test.idm;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author pedroigor
 */
@Target({METHOD, TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface IgnoreTester {

    Class<? extends IdentityConfigurationTester>[] value();

}
