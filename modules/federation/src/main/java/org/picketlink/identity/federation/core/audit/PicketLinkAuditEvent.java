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

import org.jboss.security.audit.AuditEvent;
import org.picketlink.common.util.StringUtil;

import java.util.Map;

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

    protected String httpSessionID;

    /**
     * String that represents arbitrary text that gets
     * logged at the end of the entry
     */
    protected String optionalString;

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
     *
     * @param type an instance of {@link PicketLinkAuditEventType}
     */
    public void setType(PicketLinkAuditEventType type) {
        this.type = type;
    }

    /**
     * Get the destination of the SAML request/response
     *
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
     *
     * @return
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Set the authenticated subject's name
     *
     * @param subjectName
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * Get the ID of the assertion
     *
     * @return
     */
    public String getAssertionID() {
        return assertionID;
    }

    public void setAssertionID(String assertionID) {
        this.assertionID = assertionID;
    }

    public String getHttpSessionID() {
        return httpSessionID;
    }

    public void setHttpSessionID(String httpSessionID) {
        this.httpSessionID = httpSessionID;
    }

    public String getOptionalString() {
        return optionalString;
    }

    public void setOptionalString(String optionalString) {
        this.optionalString = optionalString;
    }

    /**
     * Context path of the auditing application
     *
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
        if (StringUtil.isNotNull(httpSessionID)) {
            builder.append(httpSessionID).append(SPACE);
        }
        builder.append(super.toString());
        return builder.toString();
    }
}