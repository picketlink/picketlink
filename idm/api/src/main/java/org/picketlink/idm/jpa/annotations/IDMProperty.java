package org.picketlink.idm.jpa.annotations;

/**
 * Marks a property of an entity bean as storing a particular type of Identity Management state.
 * 
 * @author Shane Bryzak
 */
public @interface IDMProperty {
    PropertyType value();
}
