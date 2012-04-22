package org.picketlink.identity.federation.saml.v2.ac.classes;



/**
 * <p>Java class for nymType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="nymType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="anonymity"/>
 *     &lt;enumeration value="verinymity"/>
 *     &lt;enumeration value="pseudonymity"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */ 
public enum NymType {
 
    ANONYMITY("anonymity"), 
    VERINYMITY("verinymity"), 
    PSEUDONYMITY("pseudonymity");
    private final String value;

    NymType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NymType fromValue(String v) {
        for (NymType c: NymType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
