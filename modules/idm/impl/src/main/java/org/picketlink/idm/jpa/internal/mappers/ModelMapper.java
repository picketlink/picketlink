package org.picketlink.idm.jpa.internal.mappers;

/**
 * @author pedroigor
 */
public interface ModelMapper {

    boolean supports(Class<?> entityType);

    EntityMapping createMapping(Class<?> managedType, Class<?> entityType);
}
