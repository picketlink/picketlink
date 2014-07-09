package org.picketlink.rest;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;

/**
 * Provides RESTful authentication services
 *
 * @author Shane Bryzak
 */
@Path("/auth")
@RequestScoped
public class AuthenticationEndpoint {

    @Inject
    DefaultLoginCredentials credentials;

    @Inject
    Identity identity;

    /**
     * Authenticates using the username and password values passed as parameters.
     * Returns the authenticated Account instance
     *
     * @param params
     * @return
     */
    @POST
    @Path("/login")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Account login(Map<String,String> params) {
        if (!identity.isLoggedIn()) {
            credentials.setUserId(params.get("username"));
            credentials.setPassword(params.get("password"));

            if (!AuthenticationResult.SUCCESS.equals(identity.login())) {
                return null;
            }
        }

        return identity.getAccount();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Account status() {
        if (identity.isLoggedIn()) {
            return identity.getAccount();
        } else {
            return null;
        }
    }

    /**
     * Logs out the currently authenticated user
     *
     * @return
     */
    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean logout() {
        identity.logout();
        return true;
    }
}
