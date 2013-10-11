package ca.licef.proeaf.metadata.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.metadata.Metadata;
import com.sun.jersey.spi.resource.Singleton;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;


@Singleton
@Path( "/learningOpportunities" )
public class LearningOpportunityResource {

    @GET
    @Path( "count" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getLearningObjectsCount() throws Exception {
        String res = Integer.toString(Core.getInstance().getTripleStoreService().getResultsCount("getLearningOpportunities.sparql"));
        return Response.ok(res).build();
    }


    @Context
    private ServletContext context;
}
