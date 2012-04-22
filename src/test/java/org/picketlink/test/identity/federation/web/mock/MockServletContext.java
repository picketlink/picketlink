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
package org.picketlink.test.identity.federation.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Mock Servlet Context
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
@SuppressWarnings({ "unchecked", "rawtypes"})
public class MockServletContext implements ServletContext
{ 
    private Map params =  new HashMap();
    private Map attribs = new HashMap();

    public Object getAttribute(String arg0)
    { 
        return attribs.get(arg0);
    }

    public Enumeration getAttributeNames()
    { 
        return new Enumeration() 
        {
            private Iterator iter = attribs.entrySet().iterator();

            public boolean hasMoreElements()
            {
                return iter.hasNext();
            }

            public Object nextElement()
            {
                Entry<String,Object> entry =  (Entry<String, Object>) iter.next();
                return entry.getValue();
            }
        };
    }

    public ServletContext getContext(String arg0)
    { 
        throw new RuntimeException("NYI");
    }

    public String getContextPath()
    { 
        throw new RuntimeException("NYI");
    }

    public String getInitParameter(String arg0)
    { 
        return (String) params.get(arg0);
    }

    public Enumeration getInitParameterNames()
    { 
        return new Enumeration() 
        {
            private Iterator iter = params.entrySet().iterator();

            public boolean hasMoreElements()
            {
                return iter.hasNext();
            }

            public Object nextElement()
            {
                Entry<String,Object> entry =  (Entry<String, Object>) iter.next();
                return entry.getKey();
            }
        };
    }

    public int getMajorVersion()
    { 
        return 0;
    }

    public String getMimeType(String arg0)
    { 
        throw new RuntimeException("NYI");
    }

    public int getMinorVersion()
    { 
        return 0;
    }

    public RequestDispatcher getNamedDispatcher(String arg0)
    {  
        throw new RuntimeException("NYI");
    }

    public String getRealPath(String arg0)
    { 
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String arg0)
    { 
        return new RequestDispatcher()
        {

            public void include(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException
            {   
            }

            public void forward(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException
            {   
            }
        };
    }

    public URL getResource(String arg0) throws MalformedURLException
    { 
        throw new RuntimeException("NYI");
    }

    public InputStream getResourceAsStream(String arg0)
    {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(arg0);
    }

    public Set getResourcePaths(String arg0)
    {      
        throw new RuntimeException("NYI");
    }

    public String getServerInfo()
    {  
        throw new RuntimeException("NYI");
    }

    public Servlet getServlet(String arg0) throws ServletException
    {  
        throw new RuntimeException("NYI");
    }

    public String getServletContextName()
    {  
        throw new RuntimeException("NYI");
    }

    public Enumeration getServletNames()
    {  
        throw new RuntimeException("NYI");
    }

    public Enumeration getServlets()
    {
        throw new RuntimeException("NYI");
    }

    public void log(String arg0)
    { 
    }

    public void log(Exception arg0, String arg1)
    { 
    }

    public void log(String arg0, Throwable arg1)
    { 
    }

    public void removeAttribute(String arg0)
    {
        this.attribs.remove(arg0);
    }

    public void setAttribute(String arg0, Object arg1)
    { 
        this.attribs.put(arg0, arg1);
    }

    public int getEffectiveMajorVersion() {
        return 0;
    }

    public int getEffectiveMinorVersion() {
        return 0;
    }

    public boolean setInitParameter(String name, String value) {
        return false;
    }

    public Dynamic addServlet(String servletName, String className) {
        return null;
    }

    public Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;
    }
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    }
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    public void addListener(String className) {
    }

    public <T extends EventListener> void addListener(T t) {
    }

    public void addListener(Class<? extends EventListener> listenerClass) {
    }

    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public void declareRoles(String... roleNames) {
    }
}