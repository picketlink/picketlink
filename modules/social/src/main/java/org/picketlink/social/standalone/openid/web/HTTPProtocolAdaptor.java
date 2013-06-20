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
package org.picketlink.social.standalone.openid.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.picketlink.social.standalone.openid.api.OpenIDAttributeMap;
import org.picketlink.social.standalone.openid.api.OpenIDLifecycle;
import org.picketlink.social.standalone.openid.api.OpenIDLifecycleEvent;
import org.picketlink.social.standalone.openid.api.OpenIDProtocolAdapter;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDLifeCycleException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDProtocolException;

/**
 * Protocol adapter for HTTP
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 6, 2009
 */
public class HTTPProtocolAdaptor implements OpenIDProtocolAdapter, OpenIDLifecycle {
    private static Logger log = Logger.getLogger(HTTPProtocolAdaptor.class);
    private boolean trace = log.isTraceEnabled();

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private String returnURL;

    public HTTPProtocolAdaptor(HTTPOpenIDContext octx) {
        if (octx == null)
            throw new IllegalArgumentException("http openid context is null");

        this.request = octx.getRequest();
        this.response = octx.getResponse();
        this.returnURL = octx.getReturnURL();
        this.servletContext = octx.getServletContext();
    }

    public OpenIDAttributeMap getAttributeMap() {
        OpenIDAttributeMap map = new OpenIDAttributeMap();
        if ("1".equals(request.getParameter("nickname"))) {
            map.put("nickname", "1");
        }
        if ("1".equals(request.getParameter("email"))) {
            map.put("email", "1");
        }
        if ("1".equals(request.getParameter("fullname"))) {
            map.put("fullname", "1");
        }
        if ("1".equals(request.getParameter("dob"))) {
            map.put("dob", "1");
        }
        if ("1".equals(request.getParameter("gender"))) {
            map.put("gender", "1");
        }
        if ("1".equals(request.getParameter("postcode"))) {
            map.put("postcode", "1");
        }
        if ("1".equals(request.getParameter("country"))) {
            map.put("country", "1");
        }
        if ("1".equals(request.getParameter("language"))) {
            map.put("language", "1");
        }
        if ("1".equals(request.getParameter("timezone"))) {
            map.put("timezone", "1");
        }

        return map;
    }

    public String getReturnURL() {
        return this.returnURL;
    }

    /**
     * @throws OpenIDLifeCycleException
     * @see OpenIDLifecycle#handle(OpenIDLifecycleEvent)
     */
    public void handle(OpenIDLifecycleEvent event) throws OpenIDLifeCycleException {
        if (event == null)
            throw new IllegalArgumentException("event is null");

        if (event.getEventType() == OpenIDLifecycleEvent.TYPE.SESSION) {
            String attr = event.getAttributeName();
            Object attrVal = event.getAttributeValue();

            if (event.getOperation() == OpenIDLifecycleEvent.OP.ADD) {
                request.getSession().setAttribute(attr, attrVal);
            } else if (event.getOperation() == OpenIDLifecycleEvent.OP.REMOVE) {
                request.getSession().removeAttribute(attr);
            }
        }

        if (event.getEventType() == OpenIDLifecycleEvent.TYPE.SUCCESS)
            try {
                response.sendRedirect(".");
            } catch (IOException e) {
                throw new OpenIDLifeCycleException(e);
            }
    }

    /**
     * @see OpenIDLifecycle#handle(OpenIDLifecycleEvent[])
     */
    public void handle(OpenIDLifecycleEvent[] eventArr) throws OpenIDLifeCycleException {
        for (OpenIDLifecycleEvent ev : eventArr) {
            this.handle(ev);
        }
    }

    public void sendToProvider(int version, String destinationURL, Map<String, String> paramMap) throws OpenIDProtocolException {
        if (trace)
            log.trace("send to provider=" + version + "::destinationURL=" + destinationURL);

        if (version == 1) {
            try {
                response.sendRedirect(destinationURL);
                return;
            } catch (IOException e) {
                throw new OpenIDProtocolException(e);
            }
        }

        // Version != 1

        // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

        RequestDispatcher dispatcher = this.servletContext.getRequestDispatcher("/formredirection.jsp");
        request.setAttribute("parameterMap", paramMap);
        request.setAttribute("destinationUrl", destinationURL);
        try {
            dispatcher.forward(request, response);
        } catch (IOException io) {
            throw new OpenIDProtocolException(io);
        } catch (ServletException e) {
            throw new OpenIDProtocolException(e);
        }
    }

    /**
     * @see OpenIDLifecycle#getAttributeValue(String)
     */
    public Object getAttributeValue(String name) {
        return this.request.getSession().getAttribute(name);
    }
}