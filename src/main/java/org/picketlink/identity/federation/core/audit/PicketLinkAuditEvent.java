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

import java.util.Map;

import org.jboss.security.audit.AuditEvent;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * Specialized implementation of {@link AuditEvent}
 *
 * @author anil saldhana
 */
public class PicketLinkAuditEvent extends AuditEvent {

    public static final String KEY = "PicketLinkAudit";

    protected PicketLinkAuditEventType type;

    protected String destination;

    protected String subjectName;

    protected String assertionID;

    /**
     * Web Context of who is auditing
     */
    protected String whoIsAuditing;

    public PicketLinkAuditEvent(String level, Map<String, Object> map, Exception ex) {
        super(level, map, ex);
    }

    public PicketLinkAuditEvent(String level, Map<String, Object> map) {
        super(level, map);
    }

    public PicketLinkAuditEvent(String level) {
        super(level);
    }

    public PicketLinkAuditEventType getType() {
        return type;
    }

    /**
     * Set the type of audit event 
     * @param type an instance of {@link PicketLinkAuditEventType}
     */
    public void setType(PicketLinkAuditEventType type) {
        this.type = type;
    }

    /**
     * Get the destination of the SAML request/response
     * @return
     */
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Get the authenticated subject's name
     * @return
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Set the authenticated subject's name
     * @param subjectName
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * Get the ID of the assertion
     * @return
     */
    public String getAssertionID() {
        return assertionID;
    }

    public void setAssertionID(String assertionID) {
        this.assertionID = assertionID;
    }

    /**
     * Context path of the auditing application
     * @return
     */
    public String getWhoIsAuditing() {
        return whoIsAuditing;
    }

    public void setWhoIsAuditing(String whoIsAuditing) {
        this.whoIsAuditing = whoIsAuditing;
    }

    @Override
    public String toString() {
        String SPACE = " ";
        StringBuilder builder = new StringBuilder();
        if (StringUtil.isNotNull(whoIsAuditing)) {
            builder.append(whoIsAuditing).append(SPACE);
        }
        if (type != null) {
            builder.append(type.name()).append(SPACE);
        }
        if (StringUtil.isNotNull(destination)) {
            builder.append(destination).append(SPACE);
        }
        if (StringUtil.isNotNull(subjectName)) {
            builder.append(subjectName).append(SPACE);
        }
        if (StringUtil.isNotNull(assertionID)) {
            builder.append(assertionID).append(SPACE);
        }
        builder.append(super.toString());
        return builder.toString();
    }
}