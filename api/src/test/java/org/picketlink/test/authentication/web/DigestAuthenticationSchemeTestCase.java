package org.picketlink.test.authentication.web;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.HTTPDigestUtil;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Digest;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DigestAuthenticationSchemeTestCase {

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
    private HttpSession session;

    @Before
    public void onSetup() throws ServletException {
        initMocks(this);
        when(identityInstance.get()).thenReturn(identity);
        when(credentialsInstance.get()).thenReturn(credentials);
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(AuthenticationFilter.AuthType.DIGEST.name());
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("123456");
        when(request.getMethod()).thenReturn("GET");

        filter.init(config);
    }

    @Test
    public void testChallengeClient() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("WWW-Authenticate"), startsWith("Digest realm=\"" + AuthenticationFilter.DEFAULT_REALM_NAME + "\""));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();
    }

    @Test
    public void testAuthentication() throws Exception {
        final StringBuffer authenticateHeader = new StringBuffer();

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return authenticateHeader.append(invocation.getArguments()[1].toString());
            }
        }).when(response).setHeader(eq("WWW-Authenticate"), any(String.class));

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();

        String[] challengeTokens = HTTPDigestUtil.quoteTokenize(authenticateHeader.toString().replace("Digest ", ""));
        Digest clientDigest = HTTPDigestUtil.digest(challengeTokens);

        when(request.getHeader(eq("Authorization"))).thenReturn(buildAuthorizationHeader(clientDigest));
        when(credentials.getCredential()).thenReturn(clientDigest);

        filter.doFilter(request, response, filterChain);

        verify(credentials).setCredential(any(Digest.class));
        verify(identity).login();
    }

    @Test
    public void testInvalidNounce() throws Exception {
        final StringBuffer authenticateHeader = new StringBuffer();

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return authenticateHeader.append(invocation.getArguments()[1].toString());
            }
        }).when(response).setHeader(eq("WWW-Authenticate"), any(String.class));

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();

        // initialize the response mock
        response = Mockito.mock(HttpServletResponse.class);

        String[] challengeTokens = HTTPDigestUtil.quoteTokenize(authenticateHeader.toString().replace("Digest ", ""));
        Digest clientDigest = HTTPDigestUtil.digest(challengeTokens);

        clientDigest.setNonce("invalid");

        when(request.getHeader(eq("Authorization"))).thenReturn(buildAuthorizationHeader(clientDigest));

        filter.doFilter(request, response, filterChain);

        verify(credentials, never()).setCredential(any(Digest.class));
        verify(identity, never()).login();
        verify(response).setHeader(eq("WWW-Authenticate"), startsWith("Digest realm=\"" + AuthenticationFilter.DEFAULT_REALM_NAME + "\""));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testProvidedRealmName() throws Exception {
        String realmName = "My Realm";

        when(config.getInitParameter(AuthenticationFilter.REALM_NAME_INIT_PARAM)).thenReturn(realmName);

        filter.init(config);
        filter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("WWW-Authenticate"), startsWith("Digest realm=\"" + realmName + "\""));
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(identity, never()).login();
    }

    private String buildAuthorizationHeader(Digest digest) {
        String clientResponse = null;

        digest.setUsername("john");
        digest.setMethod("GET");
        digest.setUri("/dir/index.html");
        digest.setNonce(digest.getNonce());
        digest.setClientNonce(digest.getNonce());
        digest.setNonceCount("00001");

        clientResponse = HTTPDigestUtil.clientResponseValue(digest, "passwd".toCharArray());

        StringBuilder str = new StringBuilder();

        str.append("Digest ")
                .append("username=\"").append(digest.getUsername()).append("\",")
                .append("realm=\"").append(digest.getRealm()).append("\",")
                .append("nonce=\"").append(digest.getNonce()).append("\",")
                .append("uri=\"/").append(digest.getUri()).append("\",")
                .append("qop=").append(digest.getQop()).append(",")
                .append("nc=00000001,")
                .append("response=\"").append(clientResponse).append("\",")
                .append("opaque=\"").append(digest.getOpaque()).append("\"");

        return str.toString();
    }

}