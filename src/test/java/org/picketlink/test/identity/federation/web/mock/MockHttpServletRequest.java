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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Mock Http Servlet Request
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
@SuppressWarnings({ "unchecked", "rawtypes"})
public class MockHttpServletRequest implements HttpServletRequest
{
    private HttpSession session = null;
    protected Map headers = new HashMap();
    protected Map parameters = new HashMap();   
    protected Map attribs = new HashMap();

    private String methodType;

    public MockHttpServletRequest(HttpSession session, String methodType)
    {
        this.session = session;
        this.methodType = methodType;
    }

    public void addHeader(String key, String value)
    {
        headers.put(key, value);
    }

    public void addParameter(String key, String value)
    {
        parameters.put(key, value);
    }

    public String getAuthType()
    {

        throw new RuntimeException("NYI");
    }

    public String getContextPath()
    { 
        throw new RuntimeException("NYI");
    }

    public Cookie[] getCookies()
    {

        throw new RuntimeException("NYI");
    }

    public long getDateHeader(String arg0)
    { 
        return 0;
    }

    public String getHeader(String arg0)
    { 
        return (String) headers.get(arg0);
    }

    public Enumeration getHeaderNames()
    {
        return new Enumeration() 
        {
            private Iterator iter = headers.entrySet().iterator();

            public boolean hasMoreElements()
            {
                return iter.hasNext();
            }
            public Object nextElement()
            {
                Entry<String,String> entry =  (Entry<String, String>) iter.next();
                return entry.getValue();
            }
        };
    }

    public Enumeration getHeaders(String arg0)
    { 
        throw new RuntimeException("NYI");
    }

    public int getIntHeader(String arg0)
    { 
        return 0;
    }

    public String getMethod()
    { 
        return this.methodType;
    }

    public String getPathInfo()
    {

        throw new RuntimeException("NYI");
    }

    public String getPathTranslated()
    {

        throw new RuntimeException("NYI");
    }

    public String getQueryString()
    {
        if("POST".equalsIgnoreCase(this.methodType))
            return null;

        throw new RuntimeException("NYI");
    }

    public String getRemoteUser()
    {

        throw new RuntimeException("NYI");
    }

    public String getRequestURI()
    {

        throw new RuntimeException("NYI");
    }

    public StringBuffer getRequestURL()
    {

        throw new RuntimeException("NYI");
    }

    public String getRequestedSessionId()
    {

        throw new RuntimeException("NYI");
    }

    public String getServletPath()
    {

        throw new RuntimeException("NYI");
    }

    public HttpSession getSession()
    {
        return session;
    }

    public HttpSession getSession(boolean arg0)
    { 
        return getSession();
    }

    public Principal getUserPrincipal()
    {

        throw new RuntimeException("NYI");
    }

    public boolean isRequestedSessionIdFromCookie()
    {

        return false;
    }

    public boolean isRequestedSessionIdFromURL()
    {

        return false;
    }

    public boolean isRequestedSessionIdFromUrl()
    {

        return false;
    }

    public boolean isRequestedSessionIdValid()
    {

        return false;
    }

    public boolean isUserInRole(String arg0)
    {

        return false;
    }

    public Object getAttribute(String arg0)
    {

        throw new RuntimeException("NYI");
    }

    public Enumeration getAttributeNames()
    {

        throw new RuntimeException("NYI");
    }

    public String getCharacterEncoding()
    {

        throw new RuntimeException("NYI");
    }

    public int getContentLength()
    {

        return 0;
    }

    public String getContentType()
    {

        throw new RuntimeException("NYI");
    }

    public ServletInputStream getInputStream() throws IOException
    {

        throw new RuntimeException("NYI");
    }

    public String getLocalAddr()
    {

        throw new RuntimeException("NYI");
    }

    public String getLocalName()
    {

        throw new RuntimeException("NYI");
    }

    public int getLocalPort()
    {

        return 0;
    }

    public Locale getLocale()
    {

        throw new RuntimeException("NYI");
    }

    public Enumeration getLocales()
    {

        throw new RuntimeException("NYI");
    }

    public String getParameter(String arg0)
    {
        return (String) this.parameters.get(arg0);
    }

    public Map getParameterMap()
    {
        return this.parameters;
    }

    public Enumeration getParameterNames()
    {

        throw new RuntimeException("NYI");
    }

    public String[] getParameterValues(String arg0)
    {

        throw new RuntimeException("NYI");
    }

    public String getProtocol()
    {

        throw new RuntimeException("NYI");
    }

    public BufferedReader getReader() throws IOException
    {

        throw new RuntimeException("NYI");
    }

    public String getRealPath(String arg0)
    {

        throw new RuntimeException("NYI");
    }

    public String getRemoteAddr()
    {
        return (String) headers.get("Referer");
    }

    public String getRemoteHost()
    {

        throw new RuntimeException("NYI");
    }

    public int getRemotePort()
    {

        return 0;
    }

    public RequestDispatcher getRequestDispatcher(String arg0)
    {

        throw new RuntimeException("NYI");
    }

    public String getScheme()
    {

        throw new RuntimeException("NYI");
    }

    public String getServerName()
    {

        throw new RuntimeException("NYI");
    }

    public int getServerPort()
    {

        return 0;
    }

    public boolean isSecure()
    {

        return false;
    }

    public void removeAttribute(String arg0)
    {  
    }

    public void setAttribute(String arg0, Object arg1)
    {
        this.attribs.put(arg0, arg1);
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
    { 
    }

    public ServletContext getServletContext() {
        return null;
    }

    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    public boolean isAsyncStarted() {
        return false;
    }

    public boolean isAsyncSupported() {
        return false;
    }

    public AsyncContext getAsyncContext() {
        return null;
    }

    public DispatcherType getDispatcherType() {
        return null;
    }

    public boolean authenticate(HttpServletResponse response) throws IOException,
    ServletException {
        return false;
    }

    public void login(String username, String password) throws ServletException {
    }

    public void logout() throws ServletException {
    }
 
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }
}