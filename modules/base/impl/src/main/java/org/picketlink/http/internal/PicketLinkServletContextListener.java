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
package org.picketlink.http.internal;

import org.picketlink.config.SecurityConfiguration;
import org.picketlink.extension.PicketLinkExtension;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;

/**
 * <p>A {@link javax.servlet.ServletContextListener} responsible for configure the PicketLink Security Filter
 * to an application.</p>
 *
 * @author Pedro Igor
 */
@WebListener
public class PicketLinkServletContextListener implements ServletContextListener {

    public static final String PICKETLINK_SECURITY_FILTER_NAME = "PicketLink Security Filter";
    @Inject
    private PicketLinkExtension picketLinkExtension;

    @Inject
    private SecurityFilter securityFilter;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (isHttpSecurityEnabled()) {
            addSecurityFilter(sce);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private void addSecurityFilter(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        FilterRegistration.Dynamic filter = servletContext.addFilter(PICKETLINK_SECURITY_FILTER_NAME, this.securityFilter);

        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
    }

    private boolean isHttpSecurityEnabled() {
        SecurityConfiguration securityConfiguration = this.picketLinkExtension.getSecurityConfiguration();

        return securityConfiguration.getHttpSecurityConfiguration() != null;
    }
}
