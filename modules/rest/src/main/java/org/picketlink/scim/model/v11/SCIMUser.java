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
package org.picketlink.scim.model.v11;

/**
 * SCIM User Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SCIMUser extends AbstractResource {
    private UserName name;

    private Addresses[] addresses;
    private PhoneNumbers[] phoneNumbers;
    private Ims[] ims;
    private Emails[] emails;
    private Photos[] photos;
    private SCIMGroups[] groups;
    private X509Certificates[] x509Certificates;

    private String displayName;
    private String nickName;
    private String profileUrl;
    private String title;
    private String userType;
    private String preferredLanguage;
    private String locale;
    private String timezone;
    private boolean active;
    private String password;

    public UserName getName() {
        return name;
    }

    public SCIMUser setName(UserName name) {
        this.name = name;
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

    public SCIMGroups[] getGroups() {
        return groups;
    }

    public SCIMUser setGroups(SCIMGroups[] groups) {
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
        private String type;
        private String streetAddress;
        private String locality;
        private String region;
        private String postalCode;
        private String country;
        private String formatted;
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