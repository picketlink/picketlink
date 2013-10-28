package org.picketlink.test.idm;

import org.junit.Test;

/**
 * @author Pedro Igor
 */
public class BaseDNTestCase {

    String[] values = {
            "OU=Users,OU=Singapore,OU=SG,OU=APAC,DC=rim,DC=net",
            "OU=Users,OU=Singapore,OU=SG,OU=APAC,DC=rim,DC=com",
            "OU=Users,OU=Brazil,OU=SG,OU=APAC,DC=rim,DC=net",
            "OU=Roles,OU=Brazil,OU=SG,OU=APAC,DC=rim,DC=net"
    };

    @Test
    public void testMatchMiddle() {
        String pattern = "OU=Users,*,DC=rim,DC=net";

        for (String value : values) {
            if (pattern.contains("*")) {
                if (pattern.indexOf("*") != pattern.lastIndexOf("*")) {
                    throw new RuntimeException("The wildcard can appear only once [" + value + "]");
                }

                String[] parcialPatterns = pattern.split("\\*");

                if (parcialPatterns.length == 2) {
                    if (value.startsWith(parcialPatterns[0]) && value.endsWith(parcialPatterns[1])) {
                        System.out.println("Pattern match: " + pattern + " : " + value);
                    }
                }
            }
        }
    }

}
