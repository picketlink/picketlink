/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.trust.jbossws.handler;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.security.SecurityContext;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.trust.jbossws.SAML2Constants;
import org.picketlink.trust.jbossws.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>Base class for SAML handlers implementations. A default implementation is provided by the {@link SAML2Handler} class.</p>
 * 
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @author <a href="alessio.soldano@jboss.com">Alessio Soldano</a>
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 * @version $Revision: 1 $
 */
public abstract class AbstractSAML2Handler extends AbstractPicketLinkTrustHandler {
    // The system property key that can be set to determine the keys under which the roles may be in the assertion
    public static final String ROLE_KEY_SYS_PROP = "picketlink.rolekey";

    /**
     * Retrieves the SAML assertion from the SOAP payload and lets invocation go to JAAS for validation.
     */
    protected boolean handleInbound(MessageContext msgContext) {
        logger.trace("Handling Inbound Message");

        String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();
        SOAPMessageContext ctx = (SOAPMessageContext) msgContext;
        SOAPMessage soapMessage = ctx.getMessage();

        if (soapMessage == null)
            throw logger.nullValueError("SOAP Message");

        // retrieve the assertion
        Document document = soapMessage.getSOAPPart();
        Element soapHeader = Util.findOrCreateSoapHeader(document.getDocumentElement());
        Element assertion = Util.findElement(soapHeader, new QName(assertionNS, "Assertion"));
        if (assertion != null) {
            AssertionType assertionType = null;
            try {
                assertionType = SAMLUtil.fromElement(assertion);
                if (AssertionUtil.hasExpired(assertionType))
                    throw new RuntimeException(logger.samlAssertionExpiredError());
            } catch (Exception e) {
                logger.samlAssertionPasingFailed(e);
            }
            SamlCredential credential = new SamlCredential(assertion);
            if (logger.isTraceEnabled()) {
                logger.trace("Assertion included in SOAP payload: " + credential.getAssertionAsString());
            }
            Element subject = Util.findElement(assertion, new QName(assertionNS, "Subject"));
            Element nameID = Util.findElement(subject, new QName(assertionNS, "NameID"));
            String username = getUsername(nameID);
            
            // set SecurityContext
            Subject theSubject = new Subject();
            PicketLinkPrincipal principal = new PicketLinkPrincipal(username);

            createSecurityContext(credential, theSubject, principal);
            
            if (assertionType != null) {
                List<String> roleKeys = new ArrayList<String>();
                String roleKey = SecurityActions.getSystemProperty(ROLE_KEY_SYS_PROP, "Role");
                if (StringUtil.isNotNull(roleKey)) {
                    roleKeys.addAll(StringUtil.tokenize(roleKey));
                }

                logger.trace("Rolekeys to extract roles from the assertion: " + roleKeys);

                List<String> roles = AssertionUtil.getRoles(assertionType, roleKeys);
                if (roles.size() > 0) {
                    logger.trace("Roles in the assertion: " + roles);
                    Group roleGroup = SecurityActions.group(roles);
                    theSubject.getPrincipals().add(roleGroup);
                } else {
                    logger.trace("Did not find roles in the assertion");
                }
            }
        } else {
            logger.trace("We did not find any assertion");
        }
        return true;
    }

    /**
     * <p>Subclasses can override this method to customize how the security context is created.</p>
     * 
     * @param credential
     * @param theSubject
     * @param principal
     */
    protected void createSecurityContext(SamlCredential credential, Subject theSubject, Principal principal) {
        SecurityContext sc = SecurityActions.createSecurityContext(principal, credential, theSubject);
        SecurityActions.setSecurityContext(sc);
    }

    /**
     * It expects a {@link Element} assertion as the value of the {@link SAML2Constants#SAML2_ASSERTION_PROPERTY} property. This
     * assertion is then included in the SOAP payload.
     */
    protected boolean handleOutbound(MessageContext msgContext) {
        logger.trace("Handling Outbound Message");

        SOAPMessageContext ctx = (SOAPMessageContext) msgContext;
        SOAPMessage soapMessage = ctx.getMessage();

        // retrieve assertion first from the message context
        Element assertion = (Element) ctx.get(SAML2Constants.SAML2_ASSERTION_PROPERTY);

        // Assertion can also be obtained from the JAAS subject
        if (assertion == null) {
            assertion = getAssertionFromSubject();
        }

        if (assertion == null) {
            logger.trace("We did not find any assertion");
            return true;
        }

        // add wsse header
        Document document = soapMessage.getSOAPPart();
        Element soapHeader = Util.findOrCreateSoapHeader(document.getDocumentElement());
        try {
            Element wsse = getSecurityHeaderElement(document);
            wsse.setAttributeNS(soapHeader.getNamespaceURI(), soapHeader.getPrefix() + ":mustUnderstand", "1");
            if (assertion != null) {
                // add the assertion as a child of the wsse header
                // check if the assertion element comes from the same document, otherwise import the node
                if (document != assertion.getOwnerDocument()) {
                    wsse.appendChild(document.importNode(assertion, true));
                } else {
                    wsse.appendChild(assertion);
                }
            }
            soapHeader.insertBefore(wsse, soapHeader.getFirstChild());
        } catch (Exception e) {
            logger.error(e);
            return false;
        }

        return true;
    }
}