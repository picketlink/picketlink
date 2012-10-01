package org.picketlink.cdi.internal.util;

public class Strings {
    public static String unqualify(String name) {
        return unqualify(name, '.');
    }

    public static String unqualify(String name, char sep) {
        return name.substring(name.lastIndexOf(sep) + 1, name.length());
    }

    public static boolean isEmpty(String string) {
        int len;
        if (string == null || (len = string.length()) == 0) {
            return true;
        }

        for (int i = 0; i < len; i++) {
            if ((Character.isWhitespace(string.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static String toClassNameString(String sep, Object... objects) {
        if (objects.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(sep);
            if (object == null) {
                builder.append("null");
            } else {
                builder.append(object.getClass().getName());
            }
        }
        return builder.substring(sep.length());
    }

    public static String toString(Object... objects) {
        return toString(" ", objects);
    }

    public static String toString(String sep, Object... objects) {
        if (objects.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(sep).append(object);
        }
        return builder.substring(sep.length());
    }
}