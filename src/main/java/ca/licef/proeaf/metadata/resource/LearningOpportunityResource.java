package ca.licef.proeaf.metadata.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.metadata.Metadata;
import com.sun.jersey.spi.resource.Singleton;
import licef.tsapi.TripleStore;
import licef.tsapi.model.Triple;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;


@Singleton
@Path( "/learningOpportunities" )
public class LearningOpportunityResource {

    @GET
    @Path( "{uri}/lastMinuteInfos" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getLastMinuteInfos(@PathParam( "uri" ) String uri) throws Exception {
        String content = "";
        Triple[] triples = Metadata.getInstance().getLastMinuteInfos(uri);
        if (triples.length != 0)
            content = triples[0].getObject();
        return Response.ok(content).build();
    }

    @PUT
    @Path( "{uri}/lastMinuteInfos" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response setLastMinuteInfos(@PathParam( "uri" ) String uri, @FormParam( "content" ) String content) throws Exception {
        Metadata.getInstance().deleteLastMinuteInfos(uri);
        Metadata.getInstance().setLastMinuteInfos(uri, content);
        return Response.ok().build();
    }

    @DELETE
    @Path( "{uri}/lastMinuteInfos" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response deleteLastMinuteInfos(@PathParam( "uri" ) String uri) throws Exception {
        Metadata.getInstance().deleteLastMinuteInfos(uri);
        return Response.ok().build();
    }
}
