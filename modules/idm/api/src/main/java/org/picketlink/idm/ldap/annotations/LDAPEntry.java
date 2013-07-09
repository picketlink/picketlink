package org.picketlink.idm.ldap.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Pedro Igor
 *
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface LDAPEntry {
    String baseDN();
    String id();
    String[] objectClass();
}
