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
package org.picketlink.identity.federation.core.audit;

import org.jboss.security.SecurityConstants;
import org.jboss.security.audit.AuditEvent;
import org.jboss.security.audit.AuditManager;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.io.InputStream;

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
     *
     * @param securityDomainName the security domain name
     *
     * @throws ConfigurationException
     */
    public PicketLinkAuditHelper(String securityDomainName) throws ConfigurationException {
        configureAuditManager(securityDomainName);
    }

    protected void configureAuditManager(String securityDomainName) throws ConfigurationException {
        try {
            Context context = new InitialContext();
            auditManager = (AuditManager) context
                    .lookup(SecurityConstants.JAAS_CONTEXT_ROOT + securityDomainName + "/auditMgr");
        } catch (NamingException e) {
            throw logger.auditAuditManagerNotFound(SecurityConstants.JAAS_CONTEXT_ROOT + securityDomainName + "/auditMgr", e);
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
     *
     * @param servletContext
     *
     * @return
     *
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
                    throw logger.auditSecurityDomainNotFound(e1);
                }
            }
            /**
             * In the absence of /WEB-INF/jboss-web.xml,  there can be a system property
             * picketlink.audit.securitydomain    to indicate the security domain name
             */
            String secDomain = SecurityActions.getSystemProperty(GeneralConstants.AUDIT_SECURITY_DOMAIN, null);
            if (StringUtil.isNotNull(secDomain))
                return secDomain;

            throw logger.auditSecurityDomainNotFound(e);
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