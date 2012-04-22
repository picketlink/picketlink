package org.picketlink.identity.federation.saml.v2.ac.classes;
 


/**
 * <p>Java class for DeviceTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeviceTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="hardware"/>
 *     &lt;enumeration value="software"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */ 
public enum DeviceTypeType {
 
    HARDWARE("hardware"), 
    SOFTWARE("software");
    private final String value;

    DeviceTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeviceTypeType fromValue(String v) {
        for (DeviceTypeType c: DeviceTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
