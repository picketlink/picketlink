package org.picketlink.authentication.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;

/**
 * 
 * @author Shane Bryzak
 * @author Pedro Igor
 * 
 */
@ApplicationScoped
public class AuthenticationFilter implements Filter {

    private static final String DEFAULT_REALM_NAME = "PicketLink Default Realm";

    @Inject
    Instance<Identity> identityInstance;

    @Inject
    Instance<DefaultLoginCredentials> credentials;

    private Map<AuthType, HTTPAuthenticationScheme> authenticationSchemes = new HashMap<AuthType, HTTPAuthenticationScheme>();

    public enum AuthType {
        BASIC, DIGEST
    }

    private AuthType authType = AuthType.BASIC;
    private String realm = DEFAULT_REALM_NAME;

    @Override
    public void init(FilterConfig config) throws ServletException {
        String providedRealm = config.getInitParameter("realm");

        if (providedRealm != null) {
            this.realm = providedRealm;
        }

        String providedAuthType = config.getInitParameter("authType");

        if (providedAuthType != null) {
            setAuthType(providedAuthType);
        }

        this.authenticationSchemes.put(AuthType.DIGEST, new DigestAuthenticationScheme(this.realm));
        this.authenticationSchemes.put(AuthType.BASIC, new BasicAuthenticationScheme(this.realm));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
            ServletException {
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            throw new ServletException("This filter can only process HttpServletRequest requests.");
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Force session creation
        request.getSession();

        Identity identity = null;

        try {
            identity = identityInstance.get();
        } catch (Exception e) {
            throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.",
                    e);
        }

        DefaultLoginCredentials creds = null;

        try {
            creds = credentials.get();
        } catch (Exception e) {
            throw new ServletException(
                    "DefaultLoginCredentials not found - please ensure that the DefaultLoginCredentials component is created on startup.",
                    e);
        }

        HTTPAuthenticationScheme authenticationScheme = this.authenticationSchemes.get(this.authType);

        if (!identity.isLoggedIn()) {
            Object credential = authenticationScheme.extractCredential(request, response);

            if (credential != null) {
                creds.setUserId(authenticationScheme.extractUsername(request, response));
                creds.setCredential(credential);
                identity.login();
            }
        }

        if (identity.isLoggedIn()) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            authenticationScheme.challengeClient(request, response);
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    public void setAuthType(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null authentication type provided.");
        }
        
        try {
            this.authType = AuthType.valueOf(value);    
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported authentication type. Possible values are: BASIC and DIGEST.", e);
        }
    }

    public String getAuthType() {
        return this.authType.toString();
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

}
