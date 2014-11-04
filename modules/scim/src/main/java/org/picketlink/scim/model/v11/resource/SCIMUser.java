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
package org.picketlink.scim.model.v11.resource;

import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.annotations.ResourceDefinition;
import org.picketlink.scim.model.v11.Name;
import org.picketlink.scim.model.v11.ValueTypeAttribute;

/**
 * SCIM User Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
@ResourceDefinition(
    id = "urn:ietf:params:scim:schemas:core:2.0:User",
    schema = "urn:ietf:params:scim:schemas:core:2.0:User",
    name = "User",
    endpointName = "/User",
    description = "User Account"
)
public class SCIMUser extends AbstractSCIMResource {

    @ResourceAttributeDefinition(
        name = "name",
        description = "The components of the user's real name.\n" +
            "Providers MAY return just the full name as a single string in the\n" +
            "formatted sub-attribute, or they MAY return just the individual\n" +
            "component attributes using the other sub-attributes, or they MAY return\n" +
            "both. If both variants are returned, they SHOULD be describing the same\n" +
            "name, with the formatted name indicating how the component attributes\n" +
            "should be combined.")
    private Name name;

    @ResourceAttributeDefinition
    private String userName;

    @ResourceAttributeDefinition
    private String displayName;

    @ResourceAttributeDefinition
    private String nickName;

    @ResourceAttributeDefinition
    private String profileUrl;

    @ResourceAttributeDefinition
    private String title;

    @ResourceAttributeDefinition
    private String userType;

    @ResourceAttributeDefinition
    private String preferredLanguage;

    @ResourceAttributeDefinition
    private String locale;

    @ResourceAttributeDefinition
    private String timezone;

    @ResourceAttributeDefinition
    private boolean active;

    @ResourceAttributeDefinition
    private String password;

    @ResourceAttributeDefinition
    private Addresses[] addresses;

    @ResourceAttributeDefinition
    private PhoneNumbers[] phoneNumbers;

    @ResourceAttributeDefinition
    private Ims[] ims;

    @ResourceAttributeDefinition
    private Emails[] emails;

    @ResourceAttributeDefinition
    private Photos[] photos;

    @ResourceAttributeDefinition
    private SCIMGroup[] groups;

    @ResourceAttributeDefinition
    private X509Certificates[] x509Certificates;

    public Name getName() {
        return name;
    }

    public SCIMUser setName(Name name) {
        this.name = name;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public SCIMUser setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SCIMUser setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public SCIMUser setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public SCIMUser setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SCIMUser setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUserType() {
        return userType;
    }

    public SCIMUser setUserType(String userType) {
        this.userType = userType;
        return this;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public SCIMUser setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public SCIMUser setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getTimezone() {
        return timezone;
    }

    public SCIMUser setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public SCIMUser setActive(boolean active) {
        this.active = active;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SCIMUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public PhoneNumbers[] getPhoneNumbers() {
        return phoneNumbers;
    }

    public SCIMUser setPhoneNumbers(PhoneNumbers[] phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
        return this;
    }

    public Ims[] getIms() {
        return ims;
    }

    public SCIMUser setIms(Ims[] ims) {
        this.ims = ims;
        return this;
    }

    public Emails[] getEmails() {
        return emails;
    }

    public SCIMUser setEmails(Emails[] emails) {
        this.emails = emails;
        return this;
    }

    public Photos[] getPhotos() {
        return photos;
    }

    public SCIMUser setPhotos(Photos[] photos) {
        this.photos = photos;
        return this;
    }

    public SCIMGroup[] getGroups() {
        return groups;
    }

    public SCIMUser setGroups(SCIMGroup[] groups) {
        this.groups = groups;
        return this;
    }

    public X509Certificates[] getX509Certificates() {
        return x509Certificates;
    }

    public SCIMUser setX509Certificates(X509Certificates[] x509Certificates) {
        this.x509Certificates = x509Certificates;
        return this;
    }

    public Addresses[] getAddresses() {
        return addresses;
    }

    public SCIMUser setAddresses(Addresses[] addresses) {
        this.addresses = addresses;
        return this;
    }

    public static class Emails extends ValueTypeAttribute {

        @ResourceAttributeDefinition
        private boolean primary;

        public Emails() {
        }

        public boolean isPrimary() {
            return primary;
        }

        public Emails setPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }
    }

    public static class Ims extends ValueTypeAttribute {

    }

    public static class Photos extends ValueTypeAttribute {

    }

    public static class PhoneNumbers extends ValueTypeAttribute {

    }

    public static class X509Certificates {

        @ResourceAttributeDefinition
        private String value;

        public String getValue() {
            return value;
        }

        public X509Certificates setValue(String value) {
            this.value = value;
            return this;
        }
    }

    public static class Addresses {

        @ResourceAttributeDefinition
        private String type;

        @ResourceAttributeDefinition
        private String streetAddress;

        @ResourceAttributeDefinition
        private String locality;

        @ResourceAttributeDefinition
        private String region;

        @ResourceAttributeDefinition
        private String postalCode;

        @ResourceAttributeDefinition
        private String country;

        @ResourceAttributeDefinition
        private String formatted;

        @ResourceAttributeDefinition
        private boolean primary;

        public String getType() {
            return type;
        }

        public Addresses setType(String type) {
            this.type = type;
            return this;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public Addresses setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        public String getLocality() {
            return locality;
        }

        public Addresses setLocality(String locality) {
            this.locality = locality;
            return this;
        }

        public String getRegion() {
            return region;
        }

        public Addresses setRegion(String region) {
            this.region = region;
            return this;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public Addresses setPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public String getCountry() {
            return country;
        }

        public Addresses setCountry(String country) {
            this.country = country;
            return this;
        }

        public String getFormatted() {
            return formatted;
        }

        public Addresses setFormatted(String formatted) {
            this.formatted = formatted;
            return this;
        }

        public boolean isPrimary() {
            return primary;
        }

        public Addresses setPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }
    }
}