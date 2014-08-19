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
package org.picketlink.http.test;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.picketlink.Identity;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.HttpMethod;
import org.picketlink.http.internal.HttpServletRequestListener;
import org.picketlink.http.internal.SecurityFilter;
import org.picketlink.test.weld.WeldRunner;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;

import static java.util.Collections.enumeration;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@RunWith(WeldRunner.class)
public abstract class AbstractSecurityFilterTestCase {

    protected static final String CONTEXT_PATH = "/picketlink-app";

    @Inject
    protected SecurityFilter securityFilter;

    @Inject
    private HttpServletRequestListener httpServletRequestListener;

    @Inject
    protected Identity identity;

    @Inject
    protected DefaultLoginCredentials credentials;

    protected HttpSession session;
    protected FilterConfig filterConfig;
    protected RequestDispatcher requestDispatcher;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected FilterChain filterChain;

    @Before
    public void onBefore() throws Exception {
        this.filterConfig = mock(FilterConfig.class);

        this.session = mock(HttpSession.class);
        when(session.getId()).thenReturn("1");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();

                if (arguments.length == 2) {
                    when(session.getAttribute(SecurityFilter.AUTHENTICATION_ORIGINAL_PATH)).thenReturn(arguments[1]);
                }

                return null;
            }
        }).when(this.session).setAttribute(eq(SecurityFilter.AUTHENTICATION_ORIGINAL_PATH), any(PathConfiguration.class));

        this.request = mock(HttpServletRequest.class);
        this.requestDispatcher = mock(RequestDispatcher.class);
        when(request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(request.getRequestURI()).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return request.getContextPath() + request.getServletPath();
            }
        });
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getSession(anyBoolean())).thenReturn(this.session);
        when(request.getSession()).thenReturn(this.session);
        when(request.getHeaderNames()).thenReturn(enumeration(new HashSet<String>()));
        when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(this.requestDispatcher);

        ServletRequestEvent servletRequestEvent = mock(ServletRequestEvent.class);

        when(servletRequestEvent.getServletRequest()).thenReturn(this.request);

        this.response = mock(HttpServletResponse.class);
        this.filterChain = mock(FilterChain.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(response.isCommitted()).thenReturn(true);
                return null;
            }
        }).when(this.filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        this.securityFilter.init(filterConfig);
        this.httpServletRequestListener.requestInitialized(servletRequestEvent);

        when(this.request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(this.request.getMethod()).thenReturn(HttpMethod.GET.name());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(response.isCommitted()).thenReturn(true);
                return null;
            }
        }).when(this.response).sendRedirect(anyString());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(response.isCommitted()).thenReturn(true);
                return null;
            }
        }).when(this.response).sendError(anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(response.isCommitted()).thenReturn(true);
                return null;
            }
        }).when(this.response).sendError(anyInt(), anyString());
    }

    @After
    public void onAfter() {
        this.identity.logout();
    }

}
