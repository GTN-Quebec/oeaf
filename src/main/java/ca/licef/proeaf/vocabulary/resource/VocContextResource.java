package ca.licef.proeaf.vocabulary.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Security;
import ca.licef.proeaf.core.util.Util;
import ca.licef.proeaf.vocabulary.Vocabulary;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

@Singleton
@Path( "/vocContexts" )
public class VocContextResource {

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularies( @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        Hashtable<String, String>[] results =
                Core.getInstance().getTripleStore().getResults("getVocContexts.sparql");
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray vocCtxts = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                String vocCtxtUri = results[i].get("s");
                String vocUri = results[i].get("uri");
                JSONObject voc = new JSONObject();
                voc.put( "restUrl",
                        Util.getRestUrl( Constants.TYPE_VOCABULARY_CONTEXT ) + "/" +
                            Util.getIdNumberValue( vocCtxtUri ));
                voc.put( "label", Vocabulary.getInstance().getVocabularyTitle(vocUri, lang, true) );
                vocCtxts.put(voc);
            }
            json.key( "vocContexts" ).value( vocCtxts );
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

    @GET
    @Path( "{id}/used" )
    public Response isVocabularyUsed(@PathParam( "id" ) String id ) throws Exception {
        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        boolean b = Vocabulary.getInstance().getVocabularyManager().isVocabularyUsed(uri);
        return Response.ok(Boolean.toString(b)).build();
    }

    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Produces( MediaType.TEXT_HTML ) //!important for ExtJS see Ext.form.Basic.hasUpload() description -AM
    public Response addVocabulary(@Context HttpServletRequest request,
                                  @FormDataParam("name") String name,
                                  @FormDataParam("source") String source,
                                  @FormDataParam("category") String cat,
                                  @FormDataParam("navigable") String navig,
                                  @FormDataParam("url") String url,
                                  @FormDataParam("file") java.io.InputStream uploadedInputStream,
                                  @FormDataParam("file") com.sun.jersey.core.header.FormDataContentDisposition fileDetail ) throws Exception {

        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to add vocabulary.").build();

        String errorMessage = Vocabulary.getInstance().getVocabularyManager().addNewVocabulary(
                name, source, cat, "on".equals(navig), url, fileDetail.getFileName(), uploadedInputStream);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();
            json.key("success").value(errorMessage == null);
            if (errorMessage != null)
                json.key("error").value(errorMessage);
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

        return Response.ok( out.toString() ).build();
    }

    @POST
    @Path( "{id}" )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Produces( MediaType.TEXT_HTML ) //!important for ExtJS see Ext.form.Basic.hasUpload() description -AM
    public Response modifyVocabulary(@Context HttpServletRequest request,
                                     @PathParam( "id" ) String id,
                                     @FormDataParam("file") java.io.InputStream uploadedInputStream ) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to add vocabulary.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        String errorMessage = Vocabulary.getInstance().getVocabularyManager().modifyVocabularyContent(uri, uploadedInputStream);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();
            json.key("success").value(errorMessage == null);
            if (errorMessage != null)
                json.key("error").value(errorMessage);
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
        return Response.ok( out.toString() ).build();
    }

    @GET
    @Path( "{id}/details" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocContextDetails(@PathParam( "id" ) String id) throws Exception {
        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        Hashtable<String, String>[] results =
                Core.getInstance().getTripleStore().getResults("getVocContextDetails.sparql", uri);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray vocDetails = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                JSONObject detail = new JSONObject();
                detail.put( "uri", results[i].get("vocUri") );
                detail.put( "source", Util.manageQuotes( results[i].get("src")) );
                detail.put( "location", Util.manageQuotes(results[i].get("location")) );
                detail.put( "graph", Util.manageQuotes(results[i].get("graph")) );
                detail.put( "navigable", Util.manageQuotes(results[i].get("navigable")) );
                vocDetails.put(detail);
            }
            json.key( "vocDetails" ).value( vocDetails );
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

    @GET
    @Path( "{id}/update" )
    public Response resetVocabulary(@Context HttpServletRequest request, @PathParam( "id" ) String id ) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        Vocabulary.getInstance().getVocabularyManager().updateVocabulary(uri);
        return Response.ok().build();
    }

    @DELETE
    @Path( "{id}" )
    public Response deleteVocabulary(@Context HttpServletRequest request, @PathParam( "id" ) String id ) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        boolean ok = Vocabulary.getInstance().getVocabularyManager().deleteVocabulary(uri);
        if (!ok)
            return Response.status(Response.Status.UNAUTHORIZED).entity("Vocabulary linked by resource(s). Cannot delete it.").build();

        return Response.ok().build();
    }

    @GET
    @Path( "checkUpdates" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response checkUpdates(@Context HttpServletRequest request ) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to manage vocabularies.").build();

        List<String> results = Vocabulary.getInstance().getVocabularyManager().getVocsWithAvailableUpdate();
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).array();
            for (String vocCtxt : results) {
                String restUrl = Util.getRestUrl( Constants.TYPE_VOCABULARY_CONTEXT ) + "/" + Util.getIdNumberValue( vocCtxt );
                json.value(restUrl);
            }
            json.endArray();
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
    @Path( "{id}/aliases" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocContextAliases(@PathParam( "id" ) String id) throws Exception {
        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
        Hashtable<String, String>[] results = Core.getInstance().getTripleStore().getResults("getVocAliases.sparql", uri);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray vocAliases = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                JSONObject alias = new JSONObject();
                alias.put( "alias", Util.manageQuotes(results[i].get("alias")) );
                vocAliases.put(alias);
            }
            json.key( "vocAliases" ).value( vocAliases );
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

    @POST
    @Path( "{id}/aliases" )
    public Response addVocContextAlias(@Context HttpServletRequest request, @PathParam( "id" ) String id,
                                       @FormParam( "alias" ) String alias) throws Exception {

        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
//        Core.getInstance().getTripleStore().addTriple(uri, Constants.METAMODEL_VOCABULARY_ALIAS, alias);
        return Response.ok().build();
    }

    @PUT
    @Path( "{id}/aliases" )
    public Response updateVocContextAlias(@Context HttpServletRequest request,
                                          @PathParam( "id" ) String id,
                                          @FormParam( "alias" ) String alias,
                                          @FormParam( "prevAlias" ) String prevAlias) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
//        Core.getInstance().getTripleStoreService().deleteTriple(uri, Constants.METAMODEL_VOCABULARY_ALIAS, prevAlias);
//        Core.getInstance().getTripleStoreService().addTriple(uri, Constants.METAMODEL_VOCABULARY_ALIAS, alias);
        return Response.ok().build();
    }

    @DELETE
    @Path( "{id}/aliases" )
    public Response deleteVocContextAlias(@Context HttpServletRequest request, @PathParam( "id" ) String id,
                                          @FormParam( "alias" ) String alias) throws Exception {
        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
//        Core.getInstance().getTripleStoreService().deleteTriple(uri, Constants.METAMODEL_VOCABULARY_ALIAS, alias);
        return Response.ok().build();
    }

    @POST
    @Path( "{id}/navigable" )
    public Response setVocContextNav(@Context HttpServletRequest request, @PathParam( "id" ) String id) throws Exception {

        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
//        Core.getInstance().getTripleStoreService().deleteTriple(uri, Constants.METAMODEL_VOCABULARY_NAVIGABLE, "false");
//        Core.getInstance().getTripleStoreService().addTriple(uri, Constants.METAMODEL_VOCABULARY_NAVIGABLE, "true");
        return Response.ok().build();
    }

    @DELETE
    @Path( "{id}/navigable" )
    public Response deleteVocContextNav(@Context HttpServletRequest request, @PathParam( "id" ) String id) throws Exception {

        if (!Security.isAuthorized(request.getRemoteAddr()))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authorized to change settings.").build();

        String uri = Util.makeURI(id, Constants.TYPE_VOCABULARY_CONTEXT);
//        Core.getInstance().getTripleStoreService().deleteTriple(uri, Constants.METAMODEL_VOCABULARY_NAVIGABLE, "true");
//        Core.getInstance().getTripleStoreService().addTriple(uri, Constants.METAMODEL_VOCABULARY_NAVIGABLE, "false");
        return Response.ok().build();
    }

    @Context
    private ServletContext context;

}
