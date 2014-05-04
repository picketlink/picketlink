package org.picketlink.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Returns JavaScript resources
 *
 * @author Shane Bryzak
 *
 */
@Path("/script")
@ApplicationScoped
public class ScriptEndpoint {

    private String picketlinkScript;

    public ScriptEndpoint()
        throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("picketlink.js");

        byte[] buffer = new byte[4096];
        StringBuilder sb = new StringBuilder();

        int read = is.read(buffer);
        while (read != -1) {
            sb.append(new String(buffer, 0, read));
            read = is.read(buffer);
        }

        picketlinkScript = sb.toString();
    }

    @GET
    @Path("/picketlink")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPicketLinkClientScript() {
        return picketlinkScript;
    }
}
