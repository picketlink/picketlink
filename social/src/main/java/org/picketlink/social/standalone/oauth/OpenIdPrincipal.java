/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.social.standalone.oauth;

import java.io.Serializable;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A {@link Principal} representing an OpenID Authenticated principal
 *
 * @author Marcel Kolsteren
 * @author Anil Saldhana
 * @since Jan 30, 2010
 */
public class OpenIdPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 4404673070085740561L;

    private String identifier;

    private URL openIdProvider;

    private Map<String, List<String>> attributes;

    private String firstName, lastName,email, fullName;
    
    public OpenIdPrincipal(String identifier, URL openIdProvider, Map<String, List<String>> attributes) {
        super();
        this.identifier = identifier;
        this.openIdProvider = openIdProvider;
        this.attributes = attributes;
        process();
    }

    public String getName() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public URL getOpenIdProvider() {
        return openIdProvider;
    }

    public Map<String, List<String>> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "OpenIdPrincipal [identifier=" + identifier + ", openIdProvider=" + openIdProvider + ", attributes="
                + attributes + "]";
    }
    private void process(){
        if(attributes != null){
         // Try the full name
            List<String> values = attributes.get("ax_fullName");
            if (values != null && values.size() > 0){ 
                fullName = values.get(0);
            }else {
                values = attributes.get("fullname"); // Yahoo
                if (values != null && values.size() > 0){
                    fullName = values.get(0);
                } 
            }
            values = attributes.get("ax_firstName");
            if (values != null && values.size() > 0) { 
                firstName = values.get(0);
            }

            // Try the last name
            values = attributes.get("ax_lastName");
            if (values != null && values.size() > 0){
                lastName = values.get(0);
            } 
            
            if(fullName == null){
                fullName = firstName + " " + lastName;
            }
            
            // Email
            values = attributes.get("ax_email");
            if (values != null && values.size() > 0){
                email = values.get(0);
            }
        }
    }
}