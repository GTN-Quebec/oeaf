package ca.licef.proeaf.security.resource;

import ca.licef.proeaf.security.Security;
import com.sun.jersey.spi.resource.Singleton;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Path( "/" )
public class SecurityResource {

    @GET
    @Path( "isAuthorized" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response isAuthorized(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("ip") String ip) throws Exception {
        if ("".equals(ip))
            ip = request.getRemoteAddr();
        String res = Boolean.toString(Security.getInstance().isAuthorized(ip));
        return Response.ok(res).build();
    }
}
