package org.picketlink.idm.ldap.internal;

import org.picketlink.idm.IdentityManagementException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>Utility class for working with LDAP.</p>
 *
 * @author Pedro Igor
 */
public class LDAPUtil {

    /**
     * <p>Formats the given date.</p>
     *
     * @param date The Date to format.
     *
     * @return A String representing the formatted date.
     */
    public static final String formatDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("You must provide a date.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);
    }

    /**
     * <p>
     * Parses dates/time stamps stored in LDAP. Some possible values:
     * </p>
     * <ul>
     *     <li>20020228150820</li>
     *     <li>20030228150820Z</li>
     *     <li>20050228150820.12</li>
     *     <li>20060711011740.0Z</li>
     * </ul>
     *
     * @param date The date string to parse from.
     *
     * @return the Date.
     */
    public static final Date parseDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            if (date.endsWith("Z")) {
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                dateFormat.setTimeZone(TimeZone.getDefault());
            }

            return dateFormat.parse(date);
        } catch (Exception e) {
            throw new IdentityManagementException("Error converting ldap date.", e);
        }
    }


}
