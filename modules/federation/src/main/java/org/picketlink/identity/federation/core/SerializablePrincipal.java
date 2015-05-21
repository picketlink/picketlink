/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core;

import java.io.Serializable;
import java.security.Principal;

/**
 * An instance of {@link Principal} that is {@link Serializable}
 *
 * @author Anil Saldhana
 * @since Feb 21, 2012
 */
public class SerializablePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = -4732505034437816312L;

    private final String name;

    public SerializablePrincipal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    /**
     * Compare this SerializablePrincipal's name against another Principal.
     * @return true if name equals another.getName();
     */ 
    @Override
    public boolean equals(Object another)
    {
       if (!(another instanceof Principal))
          return false;
       String anotherName = ((Principal) another).getName();
       boolean equals = false;
       if (name == null)
          equals = anotherName == null;
       else
          equals = name.equals(anotherName);
       return equals;
    }

    @Override
    public int hashCode()
    {
       return (name == null ? 0 : name.hashCode());
    }

    @Override
    public String toString()
    {
       return name;
    }
}
