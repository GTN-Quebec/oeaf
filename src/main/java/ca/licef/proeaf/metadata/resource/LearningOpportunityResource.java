package ca.licef.proeaf.metadata.resource;

import com.sun.jersey.spi.resource.Singleton;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Singleton
@Path( "/learningOpportunities" )
public class LearningOpportunityResource {

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    public Response test() throws Exception {
        return Response.ok("/learningOpportunities REST service available.").build();
    }

    @GET
    @Path( "count" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getLearningObjectsCount() throws Exception {
//        String res = Integer.toString(Core.getInstance().getTripleStore().getResultsCount("getLearningOpportunities.sparql"));
//        return Response.ok(res).build();

        return null;
    }


    @Context
    private ServletContext context;
}
