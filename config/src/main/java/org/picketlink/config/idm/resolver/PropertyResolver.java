/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.config.idm.resolver;

/**
 * Interface for resolve property value from configuration (usually XML configuration) to real property value, which will be used
 * in IDM configuration.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface PropertyResolver<V> {

    /**
     * Resolve property value from configuration (usually XML configuration) to real property value, which will be used
     * in IDM configuration
     *
     * @param configurationType object from XML configuration. Type of this object is usually {@link String} (for simple text value of XML property)
     *    or {@link org.picketlink.identity.federation.core.config.idm.ObjectType} (for property value containing "Object" element with other properties
     * @param propertyClass type of property to return
     * @return real value of property, which will be used in XML configuration
     */
    V resolveProperty(Object configurationType, Class<V> propertyClass);

}
