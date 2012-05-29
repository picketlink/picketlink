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
package org.picketlink.identity.federation.bindings.tomcat.idp;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.LifecycleSupport;
import org.picketlink.identity.federation.core.ErrorCodes;

/**
 * IDP Valve for Apache Tomcat 7 and beyond
 * @author anil saldhana
 */
public class IDPWebBrowserSSOValve extends AbstractIDPValve implements Lifecycle {

    // ***************Lifecycle
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * Has this component been started yet?
     */
    private boolean started = false;

    /**
     * Add a lifecycle event listener to this component.
     * 
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    /**
     * Get the lifecycle listeners associated with this lifecycle. If this Lifecycle has no listeners registered, a zero-length
     * array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    /**
     * Remove a lifecycle event listener from this component.
     * 
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /**
     * Prepare for the beginning of active use of the public methods of this component. This method should be called after
     * <code>configure()</code>, and before any of the public methods of the component are utilized.
     * 
     * @exception LifecycleException if this component detects a fatal error that prevents this component from being used
     */
    protected synchronized void startInternal() throws LifecycleException {
        // Validate and update our current component state
        if (started)
            throw new LifecycleException(ErrorCodes.IDP_WEBBROWSER_VALVE_ALREADY_STARTED);
        super.startInternal();
        started = true;
        
        startPicketLink();
    } 

    /**
     * Gracefully terminate the active use of the public methods of this component. This method should be the last one called on
     * a given instance of this component.
     * 
     * @exception LifecycleException if this component detects a fatal error that needs to be reported
     */
    protected synchronized void stopInternal() throws LifecycleException {
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException(ErrorCodes.IDP_WEBBROWSER_VALVE_NOT_STARTED);
        super.stopInternal();
        started = false;
    }

    @Override
    protected String getContextPath() {
        return getContext().getServletContext().getContextPath();
    }
}