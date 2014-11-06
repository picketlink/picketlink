package org.picketlink.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.picketlink.Identity;

/**
 *
 * @author Shane Bryzak
 */
@Path("/auth")
@RequestScoped
public class AuthenticationEndpoint {
    @Inject Identity identity;

    @Path("/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public boolean status() {
        return identity.isLoggedIn();
    }
}
