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
import org.jboss.security.audit.AuditProvider;
import org.jboss.security.audit.providers.LogAuditProvider;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

/**
 * An implementation of {@link AuditProvider} that extends PicketBox's {@link LogAuditProvider}
 *
 * @author anil saldhana
 */
public class PicketLinkAuditProvider extends LogAuditProvider implements AuditProvider {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public PicketLinkAuditProvider() {
        super();
    }

    @Override
    public void audit(AuditEvent auditEvent) {
        if (auditEvent instanceof PicketLinkAuditEvent) {
            if (!logger.isInfoEnabled())
                return;

            PicketLinkAuditEvent picketLinkAuditEvent = (PicketLinkAuditEvent) auditEvent;
            logger.auditEvent(picketLinkAuditEvent.toString());
        } else {
            super.audit(auditEvent);
        }
    }
}