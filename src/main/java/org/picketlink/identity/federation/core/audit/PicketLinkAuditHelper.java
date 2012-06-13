/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.audit;

import java.io.InputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.jboss.security.SecurityConstants;
import org.jboss.security.audit.AuditEvent;
import org.jboss.security.audit.AuditManager;
import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class to deal with audit
 *
 * @author anil saldhana
 */
public class PicketLinkAuditHelper {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    private AuditManager auditManager = null;

    /**
     * Create a {@link PicketLinkAuditHelper}
     * @param securityDomainName the security domain name
     * @throws ConfigurationException
     */
    public PicketLinkAuditHelper(String securityDomainName) throws ConfigurationException {
        try {
            Context context = new InitialContext();
            auditManager = (AuditManager) context
                    .lookup(SecurityConstants.JAAS_CONTEXT_ROOT + securityDomainName + "/auditMgr");
        } catch (NamingException e) {
            throw logger.auditConfigurationError(e);
        }
    }

    /**
     * Audit the event
     *
     * @param ae
     */
    public void audit(AuditEvent ae) {
        if (auditManager == null) {
            throw logger.auditNullAuditManager();
        }
        auditManager.audit(ae);
    }

    /**
     * Given the servlet context, determine the security domain by which
     * the web app is secured.
     * @param servletContext
     * @return
     * @throws ConfigurationException
     */
    public static String getSecurityDomainName(ServletContext servletContext) throws ConfigurationException {
        try {
            Context context = new InitialContext();
            Object theDomain = context.lookup("java:comp/env/security/security-domain");
            return (String) theDomain;
        } catch (NamingException e) {
            // We need to fallback to see if we can find a WEB-INF/jboss-web.xml file
            InputStream is = servletContext.getResourceAsStream("/WEB-INF/jboss-web.xml");
            if (is != null) {
                try {
                    Document dom = DocumentUtil.getDocument(is);
                    return getSecurityDomainNameViaDom(dom);
                } catch (Exception e1) {
                    throw logger.auditConfigurationError(e1);
                }
            }
            /**
             * In the absence of /WEB-INF/jboss-web.xml,  there can be a system property
             * picketlink.audit.securitydomain    to indicate the security domain name
             */
            String secDomain = SecurityActions.getSystemProperty(GeneralConstants.AUDIT_SECURITY_DOMAIN, null);
            if (StringUtil.isNotNull(secDomain))
                return secDomain;

            throw logger.auditConfigurationError(e);
        }
    }

    private static String getSecurityDomainNameViaDom(Document doc) {
        Element rootNode = doc.getDocumentElement();
        NodeList nl = rootNode.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node child = nl.item(i);
            if (child instanceof Element) {
                Element el = (Element) child;
                if ("security-domain".equals(el.getNodeName())) {
                    NodeList nl1 = el.getChildNodes();
                    int len = nl1.getLength();
                    for (int j = 0; j < len; j++) {
                        Node aChild = nl1.item(j);
                        if (aChild instanceof Text) {
                            return ((Text) aChild).getNodeValue();
                        }
                    }
                }
            }
        }
        return null;
    }
}