package org.picketlink.test.authentication.web;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.ClientCertAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.X509CertificateCredentials;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClientCertAuthenticationSchemeTestCase {

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
        when(config.getInitParameter(AuthenticationFilter.AUTH_TYPE_INIT_PARAM)).thenReturn(AuthenticationFilter.AuthType.CLIENT_CERT.name());
        when(request.getMethod()).thenReturn("GET");

        filter.init(config);
    }

    @Test
    public void testChallengeClient() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "The requested resource requires a valid certificate.");
        verify(identity, never()).login();
    }

    @Test
    public void testAuthentication() throws Exception {
        X509Certificate testingCertificate = getTestingCertificate("servercert.txt");

        when(request.getAttribute(ClientCertAuthenticationScheme.X509_CLIENT_CERT_REQUEST_ATTRIBUTE))
                .thenReturn(new X509Certificate[]{testingCertificate});

        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                when(credentials.getCredential()).thenReturn(invocation.getArguments()[0]);
                return null;
            }
        }).when(credentials).setCredential(any(X509CertificateCredentials.class));

        filter.doFilter(request, response, filterChain);

        verify(identity).login();
    }

    private X509Certificate getTestingCertificate(String fromTextFile) {
        // Certificate
        InputStream bis = getClass().getClassLoader().getResourceAsStream("cert/" + fromTextFile);
        X509Certificate cert = null;

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load testing certificate.", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

}