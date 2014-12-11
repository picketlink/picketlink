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

import java.net.URI;

import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.annotations.ResourceDefinition;
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

    public static URI ID = URI.create("urn:ietf:params:scim:schemas:core:2.0:User");

    @ResourceAttributeDefinition(
            name = "userName",
            type = "string",
            multiValued = false,
            description = "Unique identifier for the User typically used by the user to directly authenticate to the service provider. Each User MUST include a non-empty userName value.  This identifier MUST be unique across the Service Consumer's entire set of Users.  REQUIRED",
            required = true,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "server"
            )
    private String userName;

    @ResourceAttributeDefinition(
            name = "name",
            type = "complex",
            multiValued = false,
            description = "The components of the user's real name. Providers MAY return just the full name as a single string in the formatted sub-attribute, or they MAY return just the individual component attributes using the other sub-attributes, or they MAY return both. If both variants are returned, they SHOULD be describing the same name, with the formatted name indicating how the component attributes should be combined.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Name name;

    @ResourceAttributeDefinition(
            name = "displayName",
            type = "string",
            multiValued = false,
            description = "The name of the User, suitable for display to end-users. The name SHOULD be the full name of the User being described if known",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String displayName;

    @ResourceAttributeDefinition(
            name = "nickName",
            type = "string",
            multiValued = false,
            description = "The casual way to address the user in real life, e.g. \"Bob\" or \"Bobby\" instead of \"Robert\".  This attribute SHOULD NOT be used to represent a User's username (e.g. bjensen or mpepperidge)",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String nickName;

    @ResourceAttributeDefinition(
            name = "profileUrl",
            type = "string",
            multiValued = false,
            description = "A fully qualified URL to a page representing the User's online profile",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String profileUrl;

    @ResourceAttributeDefinition(
            name = "title",
            type = "string",
            multiValued = false,
            description = "The user's title, such as \"Vice President.\"",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String title;

    @ResourceAttributeDefinition(
            name = "userType",
            type = "string",
            multiValued = false,
            description = "Used to identify the organization to user relationship. Typical values used might be \"Contractor\", \"Employee\", \"Intern\", \"Temp\", \"External\", and \"Unknown\" but any value may be used ",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String userType;

    @ResourceAttributeDefinition(
            name = "preferredLanguage",
            type = "string",
            multiValued = false,
            description = "Indicates the User's preferred written or spoken language.  Generally used for selecting a localized User interface. e.g., 'en_US' specifies the language English and country US.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String preferredLanguage;

    @ResourceAttributeDefinition(
            name = "locale",
            type = "string",
            multiValued = false,
            description = "Used to indicate the User's default location for purposes of localizing items such as currency, date time format, numerical representations, etc.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String locale;

    @ResourceAttributeDefinition(
            name = "timezone",
            type = "string",
            multiValued = false,
            description = "The User's time zone in the \"Olson\" timezone database format [19]; e.g.,'America/Los_Angeles'",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String timezone;

    @ResourceAttributeDefinition(
            name = "active",
            type = "boolean",
            multiValued = false,
            description = "A Boolean value indicating the User's administrative status.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private boolean active;

    @ResourceAttributeDefinition(
            name = "password",
            type = "string",
            multiValued = false,
            description = "The User's clear text password.  This attribute is intended to be used as a means to specify an initial password when creating a new User or to reset an existing User's password.",
            required = false,
            caseExact = false,
            mutability = "writeOnly",
            returned = "never",
            uniqueness = "none"
            )
    private String password;

    @ResourceAttributeDefinition(
            name = "addresses",
            type = "complex",
            multiValued = true,
            description = "A physical mailing address for this User, as described in (address Element). Canonical Type Values of work, home, and other. The value attribute is a complex type with the following sub-attributes.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Addresses[] addresses;

    @ResourceAttributeDefinition(
            name = "phoneNumbers",
            type = "complex",
            multiValued = true,
            description = "Phone numbers for the User.  The value SHOULD be canonicalized by the Service Provider according to format in RFC3966 [20] e.g. 'tel:+1-201-555-0123'.  Canonical Type values of work, home, mobile, fax, pager and other.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private PhoneNumbers[] phoneNumbers;

    @ResourceAttributeDefinition(
            name = "ims",
            type = "complex",
            multiValued = true,
            description = "Instant messaging addresses for the User.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Ims[] ims;

    @ResourceAttributeDefinition(
            name = "emails",
            type = "complex",
            multiValued = true,
            description = "E-mail addresses for the user. The value SHOULD be canonicalized by the Service Provider, e.g. bjensen@example.com instead of bjensen@EXAMPLE.COM. Canonical Type values of work, home, and other.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Emails[] emails;

    @ResourceAttributeDefinition(
            name = "photos",
            type = "complex",
            multiValued = true,
            description = "URLs of photos of the User.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Photos[] photos;

    @ResourceAttributeDefinition(
            name = "groups",
            type = "complex",
            multiValued = true,
            description = "A list of groups that the user belongs to, either thorough direct membership, nested groups, or dynamically calculated",
            required = false,
            caseExact = false,
            mutability = "readOnly",
            returned = "default",
            uniqueness = "none"
            )
    private SCIMGroup[] groups;

    @ResourceAttributeDefinition(
            name = "x509Certificates",
            type = "complex",
            multiValued = true,
            description = "A list of certificates issued to the User.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private X509Certificates[] x509Certificates;

    @ResourceAttributeDefinition(
            name = "entitlements",
            type = "complex",
            multiValued = true,
            description = "A list of entitlements for the User that represent a thing the User has.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Entitlement[] entitlements;

    @ResourceAttributeDefinition(
            name = "roles",
            type = "complex",
            multiValued = true,
            description = "A list of roles for the User that collectively represent who the User is; e.g., 'Student', 'Faculty'.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Role[] roles;

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

    public Entitlement[] getEntitlements() {
        return entitlements;
    }

    public SCIMUser setEntitlements(Entitlement[] entitlements) {
        this.entitlements = entitlements;
        return this;
    }

    public Role[] getRoles() {
        return roles;
    }

    public SCIMUser setRoles(Role[] roles) {
        this.roles = roles;
        return this;
    }

    public static class Name {

        @ResourceAttributeDefinition(
                name = "formatted",
                type = "string",
                multiValued = false,
                description = "The full name, including all middle names, titles, and suffixes as appropriate, formatted for display (e.g. Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String formatted;

        @ResourceAttributeDefinition(
                name = "familyName",
                type = "string",
                multiValued = false,
                description = "The family name of the User, or Last Name in most Western languages (e.g. Jensen given the full name Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String familyName;

        @ResourceAttributeDefinition(
                name = "givenName",
                type = "string",
                multiValued = false,
                description = "The given name of the User, or First Name in most Western languages (e.g. Barbara given the full name Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String givenName;

        @ResourceAttributeDefinition(
                name = "middleName",
                type = "string",
                multiValued = false,
                description = "The middle name(s) of the User (e.g. Robert given the full name Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String middleName;

        @ResourceAttributeDefinition(
                name = "honorificPrefix",
                type = "string",
                multiValued = false,
                description = "The honorific prefix(es) of the User, or Title in most Western languages (e.g. Ms. given the full name Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String honorificPrefix;

        @ResourceAttributeDefinition(
                name = "honorificSuffix",
                type = "string",
                multiValued = false,
                description = "The honorific suffix(es) of the User, or Suffix in most Western languages (e.g. III. given the full name Ms. Barbara J Jensen, III.).",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String honorificSuffix;

        public String getFormatted() {
            return formatted;
        }

        public Name setFormatted(String formatted) {
            this.formatted = formatted;
            return this;
        }

        public String getFamilyName() {
            return familyName;
        }

        public Name setFamilyName(String familyName) {
            this.familyName = familyName;
            return this;
        }

        public String getGivenName() {
            return givenName;
        }

        public Name setGivenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public String getMiddleName() {
            return middleName;
        }

        public Name setMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public String getHonorificPrefix() {
            return honorificPrefix;
        }

        public Name setHonorificPrefix(String honorificPrefix) {
            this.honorificPrefix = honorificPrefix;
            return this;
        }

        public String getHonorificSuffix() {
            return honorificSuffix;
        }

        public Name setHonorificSuffix(String honorificSuffix) {
            this.honorificSuffix = honorificSuffix;
            return this;
        }
    }

    public static class Emails extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "E-mail addresses for the user. The value SHOULD be canonicalized by the Service Provider, e.g. bjensen@example.com instead of bjensen@EXAMPLE.COM. Canonical Type values of work, home, and other.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function; e.g., 'work' or 'home'.",
                required = false,
                caseExact = false,
                canonicalValues = {"work", "home", "other"},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getValue() {
            return value;
        }

        public Emails setValue(String value) {
            this.value = value;
            return this;
        }

        public String getType() {
            return type;
        }

        public Emails setType(String type) {
            this.type = type;
            return this;
        }

    }

    public static class Ims extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "Instant messaging address for the User.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function; e.g., 'aim', 'gtalk', 'mobile' etc.",
                required = false,
                caseExact = false,
                canonicalValues = {"aim", "gtalk", "icq", "xmpp", "msn", "skype", "qq", "yahoo"},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getValue() {
            return value;
        }

        public Ims setValue(String value) {
            this.value = value;
            return this;
        }

        public String getType() {
            return type;
        }

        public Ims setType(String type) {
            this.type = type;
            return this;
        }

    }

    public static class Photos extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "URL of a photo of the User.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function; e.g., 'photo' or 'thumbnail'.",
                required = false,
                caseExact = false,
                canonicalValues = {"photo", "thumbnail"},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getValue() {
            return value;
        }

        public Photos setValue(String value) {
            this.value = value;
            return this;
        }

        public String getType() {
            return type;
        }

        public Photos setType(String type) {
            this.type = type;
            return this;
        }

    }

    public static class PhoneNumbers extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "Phone number of the User",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.",
                required = false,
                caseExact = false,
                canonicalValues = {"work", "home", "mobile", "fax", "pager", "other"},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getType() {
            return type;
        }

        public PhoneNumbers setType(String type) {
            this.type = type;
            return this;
        }

        public String getValue() {
            return value;
        }

        public PhoneNumbers setValue(String value) {
            this.value = value;
            return this;
        }

    }

    public static class X509Certificates extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "The value of a X509 certificate.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function.",
                required = false,
                caseExact = false,
                canonicalValues = {},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getType() {
            return type;
        }

        public X509Certificates setType(String type) {
            this.type = type;
            return this;
        }

        public String getValue() {
            return value;
        }

        public X509Certificates setValue(String value) {
            this.value = value;
            return this;
        }

    }

    public static class Entitlement extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "The value of an entitlement",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function.",
                required = false,
                caseExact = false,
                canonicalValues = {},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getValue() {
            return value;
        }

        public Entitlement setValue(String value) {
            this.value = value;
            return this;
        }

        public String getType() {
            return type;
        }

        public Entitlement setType(String type) {
            this.type = type;
            return this;
        }

    }

    public static class Role extends ValueTypeAttribute {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "The value of a role.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function.",
                required = false,
                caseExact = false,
                canonicalValues = {},
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        public String getValue() {
            return value;
        }

        public Role setValue(String value) {
            this.value = value;
            return this;
        }

        public String getType() {
            return type;
        }

        public Role setType(String type) {
            this.type = type;
            return this;
        }

    }

    public static class Addresses {

        @ResourceAttributeDefinition(
                name = "type",
                type = "string",
                multiValued = false,
                description = "A label indicating the attribute's function; e.g., 'work' or 'home'.",
                canonicalValues = {"work", "home", "other"},
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String type;

        @ResourceAttributeDefinition(
                name = "streetAddress",
                type = "string",
                multiValued = false,
                description = "The full street address component, which may include house number, street name, PO BOX, and multi-line extended street address information. This attribute MAY contain newlines.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String streetAddress;

        @ResourceAttributeDefinition(
                name = "locality",
                type = "string",
                multiValued = false,
                description = "The city or locality component.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String locality;

        @ResourceAttributeDefinition(
                name = "region",
                type = "string",
                multiValued = false,
                description = "The state or region component.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String region;

        @ResourceAttributeDefinition(
                name = "postalCode",
                type = "string",
                multiValued = false,
                description = "The zipcode or postal code component.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String postalCode;

        @ResourceAttributeDefinition(
                name = "country",
                type = "string",
                multiValued = false,
                description = "The country name component.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String country;

        @ResourceAttributeDefinition(
                name = "formatted",
                type = "string",
                multiValued = false,
                description = "The full mailing address, formatted for display or use with a mailing label. This attribute MAY contain newlines.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
        private String formatted;

        @ResourceAttributeDefinition(
                name = "primary",
                type = "string",
                multiValued = false,
                description = "A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred mailing address or primary e-mail address. The primary attribute value 'true' MUST appear no more than once.",
                required = false,
                caseExact = false,
                mutability = "readWrite",
                returned = "default",
                uniqueness = "none"
                )
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