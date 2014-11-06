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
package org.picketlink.http.test.cors;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;


/**
 * Mock servlet context.
 *
 * @author Giriraj Sharma
 */
public class MockServletContext implements ServletContext {


	//@Override
	public String getContextPath() {
		return null;
	}


	//@Override
	public ServletContext getContext(String s) {
		return null;
	}


	//@Override
	public int getMajorVersion() {
		return 0;
	}


	//@Override
	public int getMinorVersion() {
		return 0;
	}


	//@Override
	public int getEffectiveMajorVersion() {
		return 0;
	}


	//@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}


	//@Override
	public String getMimeType(String s) {
		return null;
	}


	//@Override
	public Set getResourcePaths(String s) {
		return null;
	}


	//@Override
	public URL getResource(String s) throws MalformedURLException {
		return null;
	}


	//@Override
	public InputStream getResourceAsStream(String s) {

		try {
			return new FileInputStream("src/test/resources" + s);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}


	//@Override
	public RequestDispatcher getRequestDispatcher(String s) {
		return null;
	}


	//@Override
	public RequestDispatcher getNamedDispatcher(String s) {
		return null;
	}


	//@Override
	public Servlet getServlet(String s) throws ServletException {
		return null;
	}


	//@Override
	public Enumeration getServlets() {
		return null;
	}


	//@Override
	public Enumeration getServletNames() {
		return null;
	}


	//@Override
	public void log(String s) {

	}


	//@Override
	public void log(Exception e, String s) {

	}


	//@Override
	public void log(String s, Throwable throwable) {

	}


	//@Override
	public String getRealPath(String s) {
		return null;
	}


	//@Override
	public String getServerInfo() {
		return null;
	}


	//@Override
	public String getInitParameter(String s) {
		return null;
	}


	//@Override
	public Enumeration getInitParameterNames() {
		return null;
	}


	//@Override
	public boolean setInitParameter(String s, String s2) {
		return false;
	}


	//@Override
	public Object getAttribute(String s) {
		return null;
	}


	//@Override
	public Enumeration getAttributeNames() {
		return null;
	}


	//@Override
	public void setAttribute(String s, Object o) {

	}


	//@Override
	public void removeAttribute(String s) {

	}


	//@Override
	public String getServletContextName() {
		return null;
	}


	//@Override
	public ServletRegistration.Dynamic addServlet(String s, String s2) {
		return null;
	}


	//@Override
	public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
		return null;
	}


	//@Override
	public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
		return null;
	}


	//@Override
	public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException {
		return null;
	}


	//@Override
	public ServletRegistration getServletRegistration(String s) {
		return null;
	}


	//@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return null;
	}


	//@Override
	public FilterRegistration.Dynamic addFilter(String s, String s2) {
		return null;
	}


	//@Override
	public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
		return null;
	}


	//@Override
	public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
		return null;
	}


	//@Override
	public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException {
		return null;
	}


	//@Override
	public FilterRegistration getFilterRegistration(String s) {
		return null;
	}


	//@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return null;
	}


	//@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return null;
	}


	//@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

	}


	//@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return null;
	}


	//@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return null;
	}


	//@Override
	public void addListener(String s) {

	}


	//@Override
	public <T extends EventListener> void addListener(T t) {

	}


	//@Override
	public void addListener(Class<? extends EventListener> aClass) {

	}


	//@Override
	public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException {
		return null;
	}


	//@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return null;
	}


	//@Override
	public ClassLoader getClassLoader() {
		return null;
	}


	//@Override
	public void declareRoles(String... strings) {

	}
}
