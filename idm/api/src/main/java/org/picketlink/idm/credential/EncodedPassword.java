package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class EncodedPassword {

    private char[] value;

    public EncodedPassword(char[] value) {
        this.value = value;
    }
    public EncodedPassword(String str) {
        this.value = str.toCharArray();
    }

    public char[] getValue() {
        return value;
    }

    public void clear() {
        for (int i = 0; i < value.length; i++) {
            value[i] = 0x00;
        }
        value = null;
    }
}
