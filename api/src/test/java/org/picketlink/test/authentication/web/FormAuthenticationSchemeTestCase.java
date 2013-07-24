package org.picketlink.test.authentication.web;

import java.util.Collections;
import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.FormAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FormAuthenticationSchemeTestCase {

    @InjectMocks
    private AuthenticationFilter filter;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;
    
    @Mock
    private FilterChain filterChain;
    
    @Mock
    private FilterConfig config;
    
    @Mock
    private Instance<Identity> identityInstance;
    
    @Mock
    private Instance<DefaultLoginCredentials> credentialsInstance;
    
    @Mock
    private Identity identity;
    
    @Mock
    private DefaultLoginCredentials credentials;
    
    @Before
    public void onSetup() throws ServletException {
        initMocks(this);
        when(identityInstance.get()).thenReturn(identity);
        when(credentialsInstance.get()).thenReturn(credentials);
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(AuthenticationFilter.AuthType.FORM.name());
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(session.getId()).thenReturn("1");

        filter.init(config);
    }

    @Test
    public void testChallengeClient() throws Exception {
        when(request.getRequestURI()).thenReturn("/protectedResource");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(this.requestDispatcher);

        filter.doFilter(request, response, filterChain);

        verify(request).getRequestDispatcher("/login.jsp");
        verify(requestDispatcher).forward(request, response);
        verify(identity, never()).login();
    }

    @Test
    public void testAuthentication() throws Exception {
        testChallengeClient();

        when(request.getRequestURI()).thenReturn(FormAuthenticationScheme.J_SECURITY_CHECK);
        when(request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("john");
        when(request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("123");
        when(credentials.getCredential()).thenReturn(new Password("123"));

        filter.doFilter(request, response, filterChain);

        verify(credentials).setUserId("john");
        verify(credentials).setPassword("123");
        verify(identity).login();
    }

}