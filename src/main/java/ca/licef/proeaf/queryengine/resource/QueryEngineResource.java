package ca.licef.proeaf.queryengine.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.util.Util;
import ca.licef.proeaf.queryengine.ResultEntry;
import ca.licef.proeaf.queryengine.ResultSet;
import ca.licef.proeaf.queryengine.QueryEngine;
import ca.licef.proeaf.vocabulary.Vocabulary;
import licef.tsapi.model.NodeValue;
import licef.tsapi.model.Tuple;
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
import java.util.Locale;
import java.util.ResourceBundle;

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
    public Response search( @DefaultValue( "" ) @QueryParam( "q" ) String query, @QueryParam( "isFacetInfos" ) String isFacetInfos,
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
                    .put("logo", entry.getLogo());
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

        return Response.ok(out.toString()).build();
    }

    @GET
    @Path( "genericLODetails" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getGenericLearningOpportunityDetails(@QueryParam( "uri" ) String glo,
                                                         @DefaultValue( "en" ) @QueryParam( "lang" ) String lang) throws Exception {

        String query = Util.getQuery("getGenericLODetails.sparql", glo);
        Tuple result = Core.getInstance().getTripleStore().sparqlSelect(query)[0];

        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out );

            json.object().key("uri").value(glo);

            if (result.getValue("sigle") != null)
                json.key("sigle").value(result.getValue("sigle").getContent());
            if (result.getValue("title") != null)
                json.key("title").value(result.getValue("title").getContent());
            if (result.getValue("providerLogo") != null)
                json.key("providerLogo").value(result.getValue("providerLogo").getContent());
            if (result.getValue("descr") != null)
                json.key("descr").value(result.getValue("descr").getContent());
            if (result.getValue("subject") != null)
                json.key("subject").value(result.getValue("subject").getContent());
            if (result.getValue("prealable") != null)
                json.key("prealable").value(result.getValue("prealable").getContent());
            if (result.getValue("credit") != null)
                json.key("credit").value(result.getValue("credit").getContent());
            if (result.getValue("oppType") != null) {
                String oppTypeUri = result.getValue("oppType").getContent();
                json.key("oppType").value(Vocabulary.getInstance().getLabel(oppTypeUri, lang));
            }
            if (result.getValue("educLevel") != null) {
                String educLevelUri = result.getValue("educLevel").getContent();
                json.key("educLevel").value(Vocabulary.getInstance().getLabel(educLevelUri, lang));
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

        return Response.ok(out.toString()).build();
    }

    @GET
    @Path( "concreteLOs" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getConcreteLearningOpportunities(@QueryParam( "glo" ) String glo,
                                                     @DefaultValue( "en" ) @QueryParam( "lang" ) String lang) throws Exception {

        String query = Util.getQuery("getConcreteLOs.sparql", glo);
        Tuple[] results = Core.getInstance().getTripleStore().sparqlSelect(query);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out );

            JSONArray learningOpportunities = new JSONArray();

            for (int i = 0; i < results.length; i++) {
                JSONObject clo = new JSONObject();
                clo.put("uri", results[i].getValue("s").getContent());
                clo.put( "start", results[i].getValue("start").getContent() );
                NodeValue end = results[i].getValue("end");
                if (end != null)
                    clo.put("end", end.getContent());
                NodeValue delivery = results[i].getValue("delivery");
                if (delivery != null) {
                    String deliveryMode = Vocabulary.getInstance().getLabel(delivery.getContent(), lang);
                    clo.put("deliveryMode", deliveryMode);
                }
                NodeValue perfLanguage = results[i].getValue("lang");
                if (perfLanguage != null)
                    clo.put("perfLanguage", perfLanguage.getContent());

                learningOpportunities.put(clo);
            }

            json.object().key("learningOpportunities").value( learningOpportunities );

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

        return Response.ok(out.toString()).build();
    }

    @GET
    @Path( "concreteLODetails" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getConcreteLearningOpportunityDetails(@QueryParam( "uri" ) String clo,
                                                          @DefaultValue( "en" ) @QueryParam( "lang" ) String lang) throws Exception {

        String query = Util.getQuery("getConcreteLODetails.sparql", clo);
        Tuple result = Core.getInstance().getTripleStore().sparqlSelect(query)[0];
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out );

            json.object().key("uri").value(clo);

            NodeValue pubDate = result.getValue("pubDate");
            if (pubDate != null)
                json.key("pubDate").value(pubDate.getContent());

            NodeValue lastMinInfos = result.getValue("lastMinInfos");
            if (lastMinInfos != null)
                json.key("lastMinInfos").value(lastMinInfos.getContent());

            NodeValue longitude = result.getValue("long");
            NodeValue latitude = result.getValue("lat");
            if (longitude != null && latitude != null) {
                JSONObject location = new JSONObject();
                location.put("long", longitude.getContent()).
                        put("lat", latitude.getContent());
                NodeValue descr = result.getValue("descr");
                if (descr != null)
                    location.put("description", descr.getContent());
                json.key("location").value(location);
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

        return Response.ok(out.toString()).build();
    }

    @GET
    @Path( "providers" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getProviders() throws Exception {

        String query = Util.getQuery("getProviders.sparql");
        Tuple[] results = Core.getInstance().getTripleStore().sparqlSelect(query);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray array = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                JSONObject object = new JSONObject();
                object.put( "uri", results[i].getValue("s").getContent());
                object.put( "name", results[i].getValue("name").getContent() );
                array.put(object);
            }
            json.key( "providers" ).value( array );
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

        return Response.ok(out.toString()).build();
    }

    @GET
    @Path( "performanceLanguages" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getPerformanceLanguages(@DefaultValue( "en" ) @QueryParam( "lang" ) String lang) throws Exception {
        String query = Util.getQuery("getPerformanceLanguages.sparql");
        Tuple[] results = Core.getInstance().getTripleStore().sparqlSelect(query);
        StringWriter out = new StringWriter();

        Locale.setDefault(new Locale(lang));
        try {
            ResourceBundle resBundle = ResourceBundle.getBundle("languages");
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray array = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                JSONObject object = new JSONObject();
                String _lang = results[i].getValue("lang").getContent();
                object.put( "lang", _lang);
                object.put( "name", resBundle.getString(_lang) );
                array.put(object);
            }
            json.key( "performanceLanguages" ).value( array );
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

        return Response.ok(out.toString()).build();
    }
}
