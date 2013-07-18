package org.picketlink.idm.jpa.internal.mappers;

import org.picketlink.idm.model.AttributedType;

/**
 * @author pedroigor
 */
public interface ModelMapper {

    boolean supports(Class<?> entityType);

    EntityMapping createMapping(Class<? extends AttributedType> managedType, Class<?> entityType);
}
