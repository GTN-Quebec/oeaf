package ca.licef.proeaf.queryengine.resource;

import ca.licef.proeaf.queryengine.ResultEntry;
import ca.licef.proeaf.queryengine.ResultSet;
import ca.licef.proeaf.queryengine.QueryEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.ListIterator;

@Path( "/queryEngine" )
public class QueryEngineResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    public Response test() throws Exception {
        return Response.ok("/queryEngine REST service available.").build();
    }

    @GET
    @Path( "search" )
    @Produces( MediaType.APPLICATION_JSON )
    public String search( @DefaultValue( "" ) @QueryParam( "q" ) String query, @QueryParam( "isFacetInfos" ) String isFacetInfos,
        @DefaultValue( "0" ) @QueryParam( "start" ) String strStart, @DefaultValue( "20" ) @QueryParam( "limit" ) String strLimit,
        @DefaultValue( "en" ) @QueryParam( "lang" ) String lang) {

        int start = -1;
        if( strStart != null ) {
            try {
                start = Integer.parseInt( strStart );
            }
            catch( NumberFormatException e ) {
                throw( new WebApplicationException( e, HttpServletResponse.SC_BAD_REQUEST ) );
            }
        }

        int limit = -1;
        if( strLimit != null ) {
            try {
                limit = Integer.parseInt( strLimit );
            }
            catch( NumberFormatException e ) {
                throw( new WebApplicationException( e, HttpServletResponse.SC_BAD_REQUEST ) );
            }
        }

        ResultSet rs;
        try {
            rs = QueryEngine.getInstance().search(query, "true".equals(isFacetInfos), lang, "json", start, limit);
        }
        catch( Exception e ) {
            throw( new WebApplicationException( e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR ) );
        }

        StringWriter out = new StringWriter();

        try {
            JSONWriter json = new JSONWriter( out );
        
            JSONArray learningOpportunities = new JSONArray();

            for( ListIterator it = rs.getEntries(); it.hasNext(); ) {
                ResultEntry entry = (ResultEntry)it.next();

                JSONObject learningOpportunity = new JSONObject();
                learningOpportunity.put("id", entry.getId())
                    .put("title", entry.getTitle())
                    .put("location", entry.getLocation());
                learningOpportunities.put(learningOpportunity);
            }

            json.object()
                .key( "learningOpportunities" ).value( learningOpportunities )
                .key( "totalCount" ).value( rs.getTotalRecords() );

            for( Iterator<String> it = rs.getAdditionalDataKeys(); it.hasNext(); ) {
                String key = it.next();
                Object value = rs.getAdditionalData( key );
                json.key( key ).value( value );
            }

            json.endObject();
        }
        catch( JSONException e ) {
            e.printStackTrace();
        }

        try {
            out.close();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }

        return( out.toString() );

    }
}
