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
package org.picketlink.identity.federation.core.saml.v1;

/**
 * Constants for the SAML v1.1 Specifications
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public interface SAML11Constants
{
   String ACTION = "Action";

   String ASSERTIONID = "AssertionID";

   String ASSERTION_11_NSURI = "urn:oasis:names:tc:SAML:1.0:assertion";

   String ASSERTION_ARTIFACT = "AssertionArtifact";

   String ASSERTION_ID_REF = "AssertionIDReference";

   String ATTRIBUTE_QUERY = "AttributeQuery";

   String ATTRIBUTE_NAME = "AttributeName";

   String ATTRIBUTE_NAMESPACE = "AttributeNamespace";

   String ATTRIBUTE_STATEMENT = "AttributeStatement";

   String AUDIENCE_RESTRICTION_CONDITION = "AudienceRestrictionCondition";

   String AUTHENTICATION_INSTANT = "AuthenticationInstant";

   String AUTHENTICATION_METHOD = "AuthenticationMethod";

   String AUTH_METHOD_PASSWORD = "urn:oasis:names:tc:SAML:1.0:am:password";

   String AUTH_METHOD_KERBEROS = "urn:ietf:rfc:1510";

   String AUTH_METHOD_SRP = "urn:ietf:rfc:2945";

   String AUTH_METHOD_TLS = "urn:ietf:rfc:2246";

   String AUTHENTICATION_QUERY = "AuthenticationQuery";

   String AUTHENTICATION_STATEMENT = "AuthenticationStatement";

   String AUTHORITY_BINDING = "AuthorityBinding";

   String AUTHORITY_KIND = "AuthorityKind";

   String AUTHORIZATION_DECISION_QUERY = "AuthorizationDecisionQuery";

   String AUTHORIZATION_DECISION_STATEMENT = "AuthorizationDecisionStatement";

   String BINDING = "Binding";

   String CONFIRMATION_METHOD = "ConfirmationMethod";

   String DECISION = "Decision";

   String DNS_ADDRESS = "DNSAddress";

   String EVIDENCE = "Evidence";

   String FORMAT = "Format";

   String FORMAT_EMAIL_ADDRESS = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

   String FORMAT_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";

   String IN_RESPONSE_TO = "InResponseTo";

   String IP_ADDRESS = "IPAddress";

   String ISSUER = "Issuer";

   String ISSUE_INSTANT = "IssueInstant";

   String LOCATION = "Location";

   String MAJOR_VERSION = "MajorVersion";

   String MINOR_VERSION = "MinorVersion";

   String NAME_IDENTIFIER = "NameIdentifier";

   String NAME_QUALIFIER = "NameQualifier";

   String NAMESPACE = "Namespace";

   String PROTOCOL_11_NSURI = "urn:oasis:names:tc:SAML:1.0:protocol";

   String RECIPIENT = "Recipient";

   String REQUEST = "Request";

   String REQUEST_ID = "RequestID";

   String RESOURCE = "Resource";

   String RESPONSE = "Response";

   String RESPONSE_ID = "ResponseID";

   String STATUS = "Status";

   String STATUS_CODE = "StatusCode";

   String STATUS_DETAIL = "StatusDetail";

   String STATUS_MSG = "StatusMessage";

   String TARGET = "TARGET";

   String VALUE = "Value";
}