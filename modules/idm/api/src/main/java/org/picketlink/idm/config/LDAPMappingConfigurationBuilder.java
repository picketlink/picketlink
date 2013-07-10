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

package org.picketlink.idm.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.picketlink.idm.model.AttributedType;

/**
 * @author Pedro Igor
 *
 */
public class LDAPMappingConfigurationBuilder extends
        AbstractIdentityConfigurationChildBuilder<LDAPMappingConfiguration> {

    private Class<? extends AttributedType> mappedClass;
    private Set<String> objectClasses = new HashSet<String>();
    private String baseDN;
    private String idAttributeName;
    private Map<String, String> mappedProperties = new HashMap<String, String>();

    public LDAPMappingConfigurationBuilder(Class<? extends AttributedType> attributedType, LDAPStoreConfigurationBuilder builder) {
        super(builder);
        this.mappedClass = attributedType;
    }

    @Override
    protected LDAPMappingConfiguration create() {
        return new LDAPMappingConfiguration(this.mappedClass, this.objectClasses, this.baseDN, this.idAttributeName, this.mappedProperties);
    }

    @Override
    protected void validate() {
        //TODO: Implement validate
    }

    @Override
    public Builder<LDAPMappingConfiguration> readFrom(LDAPMappingConfiguration fromConfiguration) {
        return null;  //TODO: Implement readFrom
    }

    public LDAPMappingConfigurationBuilder objectClasses(String... objectClasses) {
        this.objectClasses.addAll(Arrays.asList(objectClasses));
        return this;
    }

    public LDAPMappingConfigurationBuilder attribute(String property, String toLDAPAttribute) {
        this.mappedProperties.put(property, toLDAPAttribute);
        return this;
    }

    public LDAPMappingConfigurationBuilder attribute(String property, String toLDAPAttribute, boolean identifier) {
        attribute(property, toLDAPAttribute);

        if (identifier) {
            this.idAttributeName = toLDAPAttribute;
        }

        return this;
    }

    public LDAPMappingConfigurationBuilder baseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }
}