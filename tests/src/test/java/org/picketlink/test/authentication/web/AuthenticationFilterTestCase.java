package org.picketlink.test.authentication.web;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.test.authentication.web.mock.MockFilterConfig;
import org.picketlink.test.authentication.web.mock.MockHttpSession;
import org.picketlink.test.authentication.web.mock.MockIdentity;
import org.picketlink.test.authentication.web.mock.MockServletContext;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.picketlink.authentication.web.AuthenticationFilter.FORCE_REAUTHENTICATION_INIT_PARAM;

@RunWith(MockitoJUnitRunner.class)
@Ignore("Still failing on JDK7. Ignored until get this fixed.")
public class AuthenticationFilterTestCase {

    /**
     * Configuration that can be passed to {@link UberFireSecurityFilter#init(javax.servlet.FilterConfig)}.
     */
    protected MockFilterConfig filterConfig;

    /**
     * A mock HttpSession that mock requests can use. This value is returned as the session from the mock request.
     */
    protected MockHttpSession mockHttpSession;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected HttpServletResponse response;

    @Mock
    protected FilterChain filterChain;

    @Mock
    protected Instance<DefaultLoginCredentials> credentialsInstance;

    @Spy
    protected DefaultLoginCredentials credentials = new DefaultLoginCredentials();

    @Mock
    protected Instance<Identity> identityInstance;

    @Mock
    protected Instance<Identity> statelessIdentityInstance;

    @Spy
    protected MockIdentity identity;

    @Mock(name = "applicationPreferredAuthSchemeInstance")
    protected Instance<HTTPAuthenticationScheme> applicationPreferredAuthSchemeInstance;

    @Mock(name = "allAvailableAuthSchemesInstance")
    private Instance<HTTPAuthenticationScheme> allAvailableAuthSchemesInstance;

    @InjectMocks
    protected AuthenticationFilter authFilter;

    @Mock
    protected HTTPAuthenticationScheme authScheme;

    @Before
    public void setup() {
        filterConfig = new MockFilterConfig( new MockServletContext() );

        // useful minimum configuration. tests may overwrite these values before calling authFilter.init().
        filterConfig.initParams.put( FORCE_REAUTHENTICATION_INIT_PARAM, "true" );

        mockHttpSession = new MockHttpSession();

        when( request.getMethod() ).thenReturn( "POST" );
        when( request.getSession() ).thenReturn( mockHttpSession );
        when( request.getSession( anyBoolean() ) ).thenReturn( mockHttpSession );

        identity.setCredentials( credentials );

        when( identityInstance.get() ).thenReturn( identity );

        when( credentialsInstance.get() ).thenReturn( credentials );

        when( applicationPreferredAuthSchemeInstance.get() ).thenReturn(authScheme);
    }

    /**
     * Attempting Identity.login() in for every denied request causes the filter to throw exceptions when the browser is making
     * requests in parallel (for example, fetching images, css, or javascript from a protected URI). This test ensures the login
     * is not attempted unless the request supplied credentials recognized by the current auth scheme.
     */
    @Test
    public void filterShouldNotAttemptLoginForNonLoginRequests() throws Exception {

        authFilter.init(filterConfig);
        authFilter.doFilter(request, response, filterChain);

        verify(identity, never()).login();
    }

    @Test
    public void filterShouldAttemptLoginForLoginRequests() throws Exception {

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                credentials.setUserId("username");
                credentials.setPassword("password");
                return null;
            }
        }).when(authScheme).extractCredential(any(HttpServletRequest.class), any(DefaultLoginCredentials.class));

        authFilter.init(filterConfig);
        authFilter.doFilter(request, response, filterChain);

        verify(identity).login();
    }

}
