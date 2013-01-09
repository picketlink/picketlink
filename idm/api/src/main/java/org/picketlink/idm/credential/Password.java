package org.picketlink.idm.credential;

/**
 * Represents a text-based password credential
 * 
 * @author Shane Bryzak
 */
public class Password {

    private char[] value;

    public Password(char[] value) {
        this.value = value;
    }
    public Password(String str) {
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
