package org.picketlink.authentication.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.SimpleUser;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    Instance<Identity> identityInstance;
    @Mock
    Instance<DefaultLoginCredentials> credentialsInstance;
    @Mock
    private Identity identity;
    @Mock
    private DefaultLoginCredentials credentials;
    @Mock
    private HTTPAuthenticationScheme authenticationScheme;
    @Mock
    private HttpSession session;

    @Before
    public void setUp() throws ServletException {
        filter = new AuthenticationFilter();
        MockitoAnnotations.initMocks(this);
    }

    private void setupCredential(boolean loggedIn, String authType) throws IOException, ServletException {
        when(identity.isLoggedIn()).thenReturn(loggedIn);
        when(credentials.getCredential()).thenReturn(new SimpleUser("john"));
        when(identityInstance.get()).thenReturn(identity);
        when(credentialsInstance.get()).thenReturn(credentials);
        when(config.getInitParameter("authType")).thenReturn(authType);
        filter.init(config);
        filter.doFilter(request, response, filterChain);
    }

    @Test
    public void testBasicAuthentication() throws Exception {
        setupCredential(false, "BASIC");
        verify(identity).login();
    }

    @Test
    public void testBasicAuthenticationRealmProvided() throws Exception {
        when(config.getInitParameter("realm")).thenReturn("myrealm");
        setupCredential(false, "BASIC");
        verify(identity).login();
    }

    @Test
    public void testBasicAuthenticationUserLoggedIn() throws Exception {
        setupCredential(true, "BASIC");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDigestAuthentication() throws Exception {
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
        when(request.getSession()).thenReturn(session);
        setupCredential(false, "DIGEST");
        verify(identity).login();
    }

    @Test
    public void testDigestAuthenticationRealmProvided() throws Exception {
        when(config.getInitParameter("realm")).thenReturn("myrealm");
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
        when(request.getSession()).thenReturn(session);
        setupCredential(false, "DIGEST");
        verify(identity).login();
    }

    @Test
    public void testDigestAuthenticationUserLoggedIn() throws Exception {
        setupCredential(true, "DIGEST");
        verify(filterChain).doFilter(request, response);
    }
}
