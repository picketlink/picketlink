package org.picketlink.test.authentication.web;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
<<<<<<< HEAD
import org.picketlink.idm.model.sample.User;
=======
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.picketlink.authentication.web.AuthenticationFilter.AuthType.BASIC;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310

public class AuthenticationFilterTestCase {

    @InjectMocks
    private AuthenticationFilter filter;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
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
    
    @Mock
    private HTTPAuthenticationScheme authenticationScheme;
    
    @Before
<<<<<<< HEAD
    public void setUp() throws ServletException {
        MockitoAnnotations.initMocks(this);
    }

    private void setupCredential(boolean loggedIn, String authType) throws IOException, ServletException {
        when(identity.isLoggedIn()).thenReturn(loggedIn);
        when(credentials.getCredential()).thenReturn(new User("john"));
=======
    public void onSetup() throws ServletException {
        initMocks(this);
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
        when(identityInstance.get()).thenReturn(identity);
        when(credentialsInstance.get()).thenReturn(credentials);
    }

    @Test
    public void testUnprotectedMethod() throws Exception {
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(BASIC.name());
        when(config.getInitParameter(AuthenticationFilter.UNPROTECTED_METHODS_INIT_PARAM)).thenReturn("OPTIONS, GET");
        when(request.getMethod()).thenReturn("OPTIONS");

        filter.init(config);
        filter.doFilter(request, response, filterChain);

        verify(response, never()).sendError(HttpServletResponse.SC_UNAUTHORIZED);

        when(request.getMethod()).thenReturn("GET");

        filter.init(config);
        filter.doFilter(request, response, filterChain);

        verify(response, never()).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testProtectedMethod() throws Exception {
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(BASIC.name());
        when(config.getInitParameter(AuthenticationFilter.UNPROTECTED_METHODS_INIT_PARAM)).thenReturn("OPTIONS");
        when(request.getMethod()).thenReturn("GET");

        filter.init(config);
        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
