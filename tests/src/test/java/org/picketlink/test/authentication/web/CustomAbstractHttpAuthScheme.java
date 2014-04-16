package org.picketlink.test.authentication.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.Identity;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;

public abstract class CustomAbstractHttpAuthScheme implements HTTPAuthenticationScheme {

    private boolean hasBeenInitialized;
    private FilterConfig config;

    @Inject
    private Identity identity;


    @Override
    public void initialize(FilterConfig config) {
        this.config = config;
        hasBeenInitialized = true;
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        // these are the credentials considered valid by this testing setup
        creds.setUserId("john");
        creds.setPassword("passwd");
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getOutputStream().print("this is a client challenge response");
        response.flushBuffer();
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder resp = new StringBuilder();
        resp.append(getClass().getName());
        if (hasBeenInitialized) {
            resp.append(", initialized");
        }
        if (config != null) {
            resp.append(", has_filter_config");
        }
        if (identity != null) {
            resp.append(", has_injected_identity");
        }
        response.setContentType("text/plain");
        response.getOutputStream().print(resp.toString());
        response.flushBuffer();
        return false;
    }

    @Override
    public boolean isProtected(HttpServletRequest request) {
        return true;
    }
}
