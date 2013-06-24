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
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BasicAuthenticationSchemeTestCase {

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
    
    @Before
    public void onSetup() throws ServletException {
        initMocks(this);
        when(identityInstance.get()).thenReturn(identity);
        when(credentialsInstance.get()).thenReturn(credentials);
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(AuthenticationFilter.AuthType.BASIC.name());
        when(request.getMethod()).thenReturn("GET");

        filter.init(config);
    }

    @Test
    public void testChallengeClient() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("WWW-Authenticate"), eq("Basic realm=\"" + AuthenticationFilter.DEFAULT_REALM_NAME + "\""));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();
    }

    @Test
    public void testAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(new String("Basic " + org.picketlink.common.util.Base64.encodeBytes("john:passwd".getBytes())));
        when(credentials.getCredential()).thenReturn(new Password("passwd"));

        filter.doFilter(request, response, filterChain);

        verify(credentials).setUserId("john");
        verify(credentials).setPassword("passwd");
        verify(identity).login();
    }

    @Test
    public void testProvidedRealmName() throws Exception {
        String realmName = "My Realm";

        when(config.getInitParameter(AuthenticationFilter.REALM_NAME_INIT_PARAM)).thenReturn(realmName);

        filter.init(config);
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("WWW-Authenticate"), eq("Basic realm=\"" + realmName + "\""));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();
    }

}