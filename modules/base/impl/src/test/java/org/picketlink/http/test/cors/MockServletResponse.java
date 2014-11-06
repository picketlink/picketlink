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


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock servlet response.
 *
 * @author Giriraj Sharma
 */
public class MockServletResponse implements HttpServletResponse {


	private int status = 200;


	private final Map<String,String> headers = new HashMap<String, String>();


	//@Override
	public void addCookie(Cookie cookie) {
	}

	//@Override
	public boolean containsHeader(String s) {
		return false;
	}

	//@Override
	public String encodeURL(String s) {
		return null;
	}

	//@Override
	public String encodeRedirectURL(String s) {
		return null;
	}

	//@Override
	public String encodeUrl(String s) {
		return null;
	}

	//@Override
	public String encodeRedirectUrl(String s) {
		return null;
	}

	//@Override
	public void sendError(int i, String s) throws IOException {
	}

	//@Override
	public void sendError(int i) throws IOException {
	}

	//@Override
	public void sendRedirect(String s) throws IOException {
	}

	//@Override
	public void setDateHeader(String s, long l) {
	}

	//@Override
	public void addDateHeader(String s, long l) {
	}


	//@Override
	public Collection<String> getHeaders(String s) {
		return null;
	}


	//@Override
	public Collection<String> getHeaderNames() {
		return null;
	}


	public String getHeader(final String name) {

		return headers.get(name);
	}


	public Map<String,String> getHeaders() {

		return headers;
	}


	//@Override
	public void setHeader(String name, String value) {

		if (value == null)
			headers.remove(name);
		else
			headers.put(name, value);
	}

	//@Override
	public void addHeader(String name, String value) {

		headers.put(name, value);
	}

	//@Override
	public void setIntHeader(String s, int i) {
	}

	//@Override
	public void addIntHeader(String s, int i) {
	}

	public int getStatus() {

		return status;
	}

	//@Override
	public void setStatus(int i) {
	}

	//@Override
	public void setStatus(int i, String s) {
	}

	//@Override
	public String getCharacterEncoding() {
		return null;
	}

	//@Override
	public String getContentType() {
		return null;
	}

	//@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	//@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	//@Override
	public void setCharacterEncoding(String s) {
	}

	//@Override
	public void setContentLength(int i) {
	}

	//@Override
	public void setContentType(String s) {
	}

	//@Override
	public void setBufferSize(int i) {
	}

	//@Override
	public int getBufferSize() {
		return 0;
	}

	//@Override
	public void flushBuffer() throws IOException {
	}

	//@Override
	public void resetBuffer() {
	}

	//@Override
	public boolean isCommitted() {
		return false;
	}

	//@Override
	public void reset() {
	}

	//@Override
	public void setLocale(Locale locale) {
	}

	//@Override
	public Locale getLocale() {
		return null;
	}
}
