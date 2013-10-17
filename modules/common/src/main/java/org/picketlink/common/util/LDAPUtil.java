package org.picketlink.common.util;

/**
 * <p>Utility class for LDAP-related code.</p>
 *
 * @author Pedro Igor
 */
public final class LDAPUtil {

    /**
     * <p>Creates a byte-based {@link String} representation of a raw byte array representing the value of the
     * <code>objectGUID</code> attribute retrieved from Active Directory.</p>
     *
     * <p>The returned string is useful to perform queries on AD based on the <code>objectGUID</code> value. Eg.:</p>
     *
     * <p>
     * String filter = "(&(objectClass=*)(objectGUID" + EQUAL + convertObjectGUIToByteString(objectGUID) + "))";
     * </p>
     *
     * @param objectGUID A raw byte array representing the value of the <code>objectGUID</code> attribute retrieved from
     * Active Directory.
     *
     * @return A byte-based String representation in the form of \[0]\[1]\[2]\[3]\[4]\[5]\[6]\[7]\[8]\[9]\[10]\[11]\[12]\[13]\[14]\[15]
     */
    public static String convertObjectGUIToByteString(byte[] objectGUID) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < objectGUID.length; i++) {
            String transformed = prefixZeros((int) objectGUID[i] & 0xFF);
            result.append("\\");
            result.append(transformed);
        }

        return result.toString();
    }

    /**
     * <p>Decode a raw byte array representing the value of the <code>objectGUID</code> attribute retrieved from Active
     * Directory.</p>
     *
     * <p>The returned string is useful to directly bind an entry. Eg.:</p>
     *
     * <p>
     * String bindingString = decodeObjectGUID(objectGUID);
     * <br/>
     * Attributes attributes = ctx.getAttributes(bindingString);
     * </p>
     *
     * @param objectGUID A raw byte array representing the value of the <code>objectGUID</code> attribute retrieved from
     * Active Directory.
     *
     * @return A string representing the decoded value in the form of [3][2][1][0]-[5][4]-[7][6]-[8][9]-[10][11][12][13][14][15].
     */
    public static String decodeObjectGUID(byte[] objectGUID) {
        StringBuilder displayStr = new StringBuilder();

        displayStr.append(convertToDashedString(objectGUID));

        return displayStr.toString();
    }

    private static String convertToDashedString(byte[] objectGUID) {
        StringBuilder displayStr = new StringBuilder();

        displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));

        return displayStr.toString();
    }

    private static String prefixZeros(int value) {
        if (value <= 0xF) {
            StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));
            return sb.toString();
        } else {
            return Integer.toHexString(value);
        }
    }

}
