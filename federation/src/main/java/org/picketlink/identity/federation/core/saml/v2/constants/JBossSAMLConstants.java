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
package org.picketlink.identity.federation.core.saml.v2.constants;

/**
 * SAML Constants
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public enum JBossSAMLConstants {
    ADDRESS("Address"), ADDITIONAL_METADATA_LOCATION("AdditionalMetadataLocation"), AFFILIATION_DESCRIPTOR(
            "AffiliationDescriptor"), ALLOW_CREATE("AllowCreate"), ARTIFACT("Artifact"), ARTIFACT_RESOLVE("ArtifactResolve"), ARTIFACT_RESPONSE(
            "ArtifactResponse"), ARTIFACT_RESOLUTION_SERVICE("ArtifactResolutionService"), ASSERTION("Assertion"), ASSERTION_CONSUMER_SERVICE(
            "AssertionConsumerService"), ASSERTION_CONSUMER_SERVICE_URL("AssertionConsumerServiceURL"), ASSERTION_CONSUMER_SERVICE_INDEX(
            "AssertionConsumerServiceIndex"), ASSERTION_ID_REQUEST_SERVICE("AssertionIDRequestService"), ATTRIBUTE("Attribute"), ATTRIBUTE_QUERY(
            "AttributeQuery"), ATTRIBUTE_AUTHORITY_DESCRIPTOR("AttributeAuthorityDescriptor"), ATTRIBUTE_CONSUMING_SERVICE(
            "AttributeConsumingService"), ATTRIBUTE_CONSUMING_SERVICE_INDEX("AttributeConsumingServiceIndex"), ATTRIBUTE_SERVICE(
            "AttributeService"), ATTRIBUTE_STATEMENT("AttributeStatement"), ATTRIBUTE_VALUE("AttributeValue"), AUDIENCE(
            "Audience"), AUDIENCE_RESTRICTION("AudienceRestriction"), AUTHN_CONTEXT("AuthnContext"), AUTHENTICATING_AUTHORITY(
            "AuthenticatingAuthority"), AUTHN_AUTHORITY_DESCRIPTOR("AuthnAuthorityDescriptor"), AUTHN_CONTEXT_CLASS_REF(
            "AuthnContextClassRef"), AUTHN_CONTEXT_DECLARATION("AuthnContextDecl"), AUTHN_CONTEXT_DECLARATION_REF(
            "AuthnContextDeclRef"), AUTHN_INSTANT("AuthnInstant"), AUTHN_REQUEST("AuthnRequest"), AUTHN_STATEMENT(
            "AuthnStatement"), AUTHN_REQUESTS_SIGNED("AuthnRequestsSigned"), BASEID("BaseID"), BINDING("Binding"), CACHE_DURATION(
            "cacheDuration"), COMPANY("Company"), CONDITIONS("Conditions"), CONSENT("Consent"), CONTACT_PERSON("ContactPerson"), CONTACT_TYPE(
            "contactType"), DESTINATION("Destination"), DNS_NAME("DNSName"), EMAIL_ADDRESS("EmailAddress"), ENCODING("Encoding"), ENCRYPTED_ASSERTION(
            "EncryptedAssertion"), ENCRYPTED_ID("EncryptedID"), ENTITY_ID("entityID"), ENTITY_DESCRIPTOR("EntityDescriptor"), ENTITIES_DESCRIPTOR(
            "EntitiesDescriptor"), EXTENSIONS("Extensions"), FORMAT("Format"), FRIENDLY_NAME("FriendlyName"), FORCE_AUTHN(
            "ForceAuthn"), GIVEN_NAME("GivenName"), ID("ID"), IDP_SSO_DESCRIPTOR("IDPSSODescriptor"), INDEX("index"), INPUT_CONTEXT_ONLY(
            "InputContextOnly"), IN_RESPONSE_TO("InResponseTo"), ISDEFAULT("isDefault"), IS_REQUIRED("isRequired"), IS_PASSIVE(
            "IsPassive"), ISSUE_INSTANT("IssueInstant"), ISSUER("Issuer"), KEY_DESCRIPTOR("KeyDescriptor"), KEY_INFO("KeyInfo"), LANG(
            "lang"), LANG_EN("en"), LOCATION("Location"), LOGOUT_REQUEST("LogoutRequest"), LOGOUT_RESPONSE("LogoutResponse"), MANAGE_NAMEID_SERVICE(
            "ManageNameIDService"), METADATA_MIME("application/samlmetadata+xml"), METHOD("Method"), NAME("Name"), NAME_FORMAT(
            "NameFormat"), NAMEID("NameID"), NAMEID_FORMAT("NameIDFormat"), NAMEID_MAPPING_SERVICE("NameIDMappingService"), NAMEID_POLICY(
            "NameIDPolicy"), NAME_QUALIFIER("NameQualifier"), NOT_BEFORE("NotBefore"), NOT_ON_OR_AFTER("NotOnOrAfter"), ORGANIZATION(
            "Organization"), ORGANIZATION_NAME("OrganizationName"), ORGANIZATION_DISPLAY_NAME("OrganizationDisplayName"), ORGANIZATION_URL(
            "OrganizationURL"), PDP_DESCRIPTOR("PDPDescriptor"), PROTOCOL_BINDING("ProtocolBinding"), PROTOCOL_SUPPORT_ENUMERATION(
            "protocolSupportEnumeration"), PROVIDER_NAME("ProviderName"), REQUESTED_AUTHN_CONTEXT("RequestedAuthnContext"), REASON(
            "Reason"), RECIPIENT("Recipient"), REQUEST("Request"), REQUESTED_ATTRIBUTE("RequestedAttribute"), REQUEST_ABSTRACT(
            "RequestAbstract"), RESPONSE("Response"), RESPONSE_LOCATION("ResponseLocation"), RETURN_CONTEXT("ReturnContext"), SESSION_INDEX(
            "SessionIndex"), SERVICE_NAME("ServiceName"), SERVICE_DESCRIPTION("ServiceDescription"), SP_PROVIDED_ID(
            "SPProvidedID"), SP_NAME_QUALIFIER("SPNameQualifier"), SP_SSO_DESCRIPTOR("SPSSODescriptor"), SIGNATURE("Signature"), SIGNATURE_SHA1_WITH_DSA(
            "http://www.w3.org/2000/09/xmldsig#dsa-sha1"), SIGNATURE_SHA1_WITH_RSA("http://www.w3.org/2000/09/xmldsig#rsa-sha1"), SINGLE_SIGNON_SERVICE(
            "SingleSignOnService"), SINGLE_LOGOUT_SERVICE("SingleLogoutService"), STATEMENT("Statement"), STATUS("Status"), STATUS_CODE(
            "StatusCode"), STATUS_DETAIL("StatusDetail"), STATUS_MESSAGE("StatusMessage"), STATUS_RESPONSE_TYPE(
            "StatusResponseType"), SUBJECT("Subject"), SUBJECT_CONFIRMATION("SubjectConfirmation"), SUBJECT_CONFIRMATION_DATA(
            "SubjectConfirmationData"), SUBJECT_LOCALITY("SubjectLocality"), SURNAME("SurName"), TELEPHONE_NUMBER(
            "TelephoneNumber"), TYPE("type"), USE("use"), VALUE("Value"), VALID_UNTIL("validUntil"), VERSION("Version"), VERSION_2_0(
            "2.0"), WANT_AUTHN_REQUESTS_SIGNED("WantAuthnRequestsSigned"), WANT_ASSERTIONS_SIGNED("WantAssertionsSigned"), XACML_AUTHZ_DECISION_QUERY(
            "XACMLAuthzDecisionQuery"), XACML_AUTHZ_DECISION_QUERY_TYPE("XACMLAuthzDecisionQueryType"), XACML_AUTHZ_DECISION_STATEMENT_TYPE(
            "XACMLAuthzDecisionStatementType"), HTTP_POST_BINDING("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"), ONE_TIME_USE ("OneTimeUse");

    private String name;

    private JBossSAMLConstants(String val) {
        this.name = val;
    }

    public String get() {
        return this.name;
    }
}