package ca.licef.proeaf.metadata.resource;

//import ca.licef.proeaf.queryengine.ResultSet;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.json.JSONWriter;
//
//import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
//import java.io.IOException;
//import java.io.Serializable;
//import java.io.StringWriter;
//import java.util.Iterator;
//import java.util.ListIterator;
import java.util.UUID;

import ca.licef.proeaf.metadata.Harvester;

@Singleton
@Path( "/harvester" )
public class HarvesterResource /*implements Serializable*/ {

    //private static final long serialVersionUID = 1L;

    @GET
    @Path( "status" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getStatus() throws Exception {
        System.out.println( "getStatus" );
        String msg = "";
        if( harvester == null )
            msg += "NO_HARVEST";
        else if( !harvester.isRunning() )
            msg += "HARVEST_TERMINATED";
        else
            msg += "HARVEST_IN_PROGRESS";

        return Response.ok( msg ).build();
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    public Response launchNewHarvest( @FormParam( "name" ) String name, @FormParam( "url" ) String url, @FormParam( "type" ) String type ) throws Exception {
        System.out.println( "launchNewHarvest name=" + name + " url=" + url + " type=" + type );
        if( harvester == null || !harvester.isRunning() ) {
            harvester = new Harvester( name, url, type );
            harvester.start();
            return Response.ok( "{ 'success': true, 'msg': 'Started' }" ).build();
        }

        return( Response.status( Response.Status.SERVICE_UNAVAILABLE ).entity( "{ 'success': true, 'msg': 'Harvester is already running.' }" ).build() );
    }

    private Harvester harvester;

}
