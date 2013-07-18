package org.picketlink.idm.jpa.internal.mappers;

import java.util.List;

/**
 * @author pedroigor
 */
public interface ModelMapper {

    boolean supports(Class<?> entityType);

    List<EntityMapping> createMapping(Class<?> entityType);
}
