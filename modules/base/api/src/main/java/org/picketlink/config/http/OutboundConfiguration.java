/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.config.http;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition;

/**
 * @author Pedro Igor
 */
public class OutboundConfiguration {

    private final PathConfiguration pathConfiguration;
    private final List<OutboundRedirectConfiguration> redirects;

    public OutboundConfiguration(PathConfiguration pathConfiguration, List<OutboundRedirectConfiguration> redirects) {
        this.pathConfiguration = pathConfiguration;

        if (redirects == null) {
            redirects = emptyList();
        }

        this.redirects = redirects;
    }

    public List<OutboundRedirectConfiguration> getRedirects() {
        return Collections.unmodifiableList(this.redirects);
    }

    public String getRedirectUrl(Condition condition) {
        for (OutboundRedirectConfiguration redirectConfiguration : this.redirects) {
            if (condition.equals(redirectConfiguration.getCondition())) {
                return redirectConfiguration.getRedirectUrl();
            }
        }

        return null;
    }

    public boolean hasRedirectWhen(Condition condition) {
        for (OutboundRedirectConfiguration redirectConfiguration : this.redirects) {
            if (condition.equals(redirectConfiguration.getCondition())) {
                return true;
            }
        }

        return false;
    }

}
