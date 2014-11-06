/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.http.internal.cors.util;

import java.util.Properties;

/**
 * Provides typed retrieval of java.util.Properties as boolean, int, long, float, double, string or enum values.
 *
 * @author Giriraj Sharma
 */
public class PropertyRetriever {

    /**
     * The property hashtable to parse.
     */
    private Properties props;

    /**
     * Creates a new retriever for the specified properties.
     *
     * @param props The properties hasthtable.
     */
    public PropertyRetriever(final Properties props) {

        this.props = props;
    }

    /**
     * Retrieves a boolean value.
     *
     * @param key The property name.
     *
     * @return The property as a boolean value.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public boolean getBoolean(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        if (value.equalsIgnoreCase("true"))
            return true;

        else if (value.equalsIgnoreCase("false"))
            return false;

        else
            throw new PropertyParseException("Invalid boolean property", key, value);
    }

    /**
     * Retrieves an optional boolean value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a boolean.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public boolean getOptBoolean(final String key, final boolean def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        if (value.equalsIgnoreCase("true"))
            return true;

        else if (value.equalsIgnoreCase("false"))
            return false;

        else
            throw new PropertyParseException("Invalid boolean property", key, value);
    }

    /**
     * Retrieves an integer value.
     *
     * @param key The property name.
     *
     * @return The property as an integer.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public int getInt(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid int property", key, value);
        }
    }

    /**
     * Retrieves an optional integer value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as an integer.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public int getOptInt(final String key, final int def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid int property", key);
        }
    }

    /**
     * Retrieves a long value.
     *
     * @param key The property name.
     *
     * @return The property as a long.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public long getLong(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        try {
            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid long property", key, value);
        }
    }

    /**
     * Retrieves an optional long value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a long.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public long getOptLong(final String key, final long def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        try {
            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid long property", key, value);
        }
    }

    /**
     * Retrieves a float value.
     *
     * @param key The property name.
     *
     * @return The property as a float.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public float getFloat(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        try {
            return Float.parseFloat(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid float property", key, value);
        }
    }

    /**
     * Retrieves an optional float value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a float.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public float getOptFloat(final String key, final float def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        try {
            return Float.parseFloat(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid float property", key, value);
        }
    }

    /**
     * Retrieves a double value.
     *
     * @param key The property name.
     *
     * @return The property as a double.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public double getDouble(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        try {
            return Double.parseDouble(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid double property", key, value);
        }
    }

    /**
     * Retrieves an optional double value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a double.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public double getOptDouble(final String key, final double def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        try {
            return Double.parseDouble(value);

        } catch (NumberFormatException e) {
            throw new PropertyParseException("Invalid double property", key, value);
        }
    }

    /**
     * Retrieves a string value.
     *
     * @param key The property name.
     *
     * @return The property as a string.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public String getString(final String key) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        return value;
    }

    /**
     * Retrieves an optional string value.
     *
     * @param key The property name.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a string.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public String getOptString(final String key, final String def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        return value;
    }

    /**
     * Retrieves an enumerated string value. String case is ignored during comparison.
     *
     * @param key The property name.
     * @param enums A string array defining the acceptable values.
     *
     * @return The property as a string.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public String getEnumString(final String key, final String[] enums) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        for (String en : enums) {

            if (en.equalsIgnoreCase(value))
                return value;
        }

        throw new PropertyParseException("Invalid enum string property", key, value);
    }

    /**
     * Retrieves an enumerated string value. String case is ignored during comparison.
     *
     * @param key The property name.
     * @param enums A string array defining the acceptable values.
     * @param def The default value if the property is undefined.
     *
     * @return The property as a string.
     *
     * @throws PropertyParseException On an invalid property.
     */
    public String getOptEnumString(final String key, final String[] enums, final String def) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        for (String en : enums) {

            if (en.equalsIgnoreCase(value))
                return value;
        }

        throw new PropertyParseException("Invalid enum string property", key, value);
    }

    /**
     * Retrieves an enumerated constant. String case is ignored during comparison.
     *
     * @param key The property name.
     * @param enumClass The enumeration class specifying the acceptable values.
     *
     * @return The matching enumerated constant.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public <T extends Enum<T>> T getEnum(final String key, final Class<T> enumClass) throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            throw new PropertyParseException("Missing property", key);

        for (T en : enumClass.getEnumConstants()) {

            if (en.toString().equalsIgnoreCase(value))
                return en;
        }

        // No match? -> raise exception
        throw new PropertyParseException("Invalid enum property", key, value);
    }

    /**
     * Retrieves an optional enumerated constant. String case is ignored during comparison.
     *
     * @param key The property name.
     * @param enumClass The enumeration class specifying the acceptable values.
     * @param def The default value if the property is undefined.
     *
     * @return The matching enumerated constant.
     *
     * @throws PropertyParseException On a missing or invalid property.
     */
    public <T extends Enum<T>> T getOptEnum(final String key, final Class<T> enumClass, final T def)
            throws PropertyParseException {

        String value = props.getProperty(key);

        if (value == null)
            return def;

        for (T en : enumClass.getEnumConstants()) {

            if (en.toString().equalsIgnoreCase(value))
                return en;
        }

        // No match? -> raise exception
        throw new PropertyParseException("Invalid enum property", key, value);
    }
}