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

package org.picketlink.idm.config.internal.resolver;

import java.util.Map;
import java.util.Properties;

import org.picketlink.config.idm.ObjectType;
import org.picketlink.idm.SecurityConfigurationException;

/**
 * resolver for creating {@link java.util.Properties} from passed ObjectType
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class JavaPropertiesResolver extends BasePropertyResolver<Properties> {

    @Override
    protected Properties resolvePropertyFromString(String stringPropertyValue, Class<Properties> propertyClass) {
        throw new SecurityConfigurationException("Not implemented");
    }

    @Override
    protected Properties resolvePropertyFromObjectType(ObjectType objectType) {
        Properties result = new Properties();

        Map<String, Object> props = objectType.getAllProperties();
        for (String propertyName : props.keySet()) {
            String propertyValue = (String)props.get(propertyName);
            result.setProperty(propertyName, propertyValue);
        }

        return result;
    }
}
