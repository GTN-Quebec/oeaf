package ca.licef.proeaf.vocabulary.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.vocabulary.Vocabulary;
import com.sun.jersey.spi.resource.Singleton;
import licef.StringUtil;
import licef.tsapi.TripleStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Hashtable;


@Singleton
@Path( "/voc" )
public class VocabularyResource {


    @GET
    @Path( "{source}/{cat}/{concept}" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getVocabularyConcept( @PathParam( "source" ) String source, @PathParam( "cat" ) String cat, @PathParam( "concept" ) String concept ) throws Exception {
        source = URLDecoder.decode(source, "UTF-8");
        cat = URLDecoder.decode(cat, "UTF-8");
        concept = URLDecoder.decode(concept, "UTF-8");
        String uri = Vocabulary.getInstance().getConcept(source, cat, concept);
        if (uri == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        else
            return Response.ok(uri).build();
    }

    @GET
    @Path( "{source}/{concept}" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response findVocabularyConcept( @PathParam( "source" ) String source, @PathParam( "concept" ) String concept ) throws Exception {
        source = URLDecoder.decode(source, "UTF-8");
        concept = URLDecoder.decode(concept, "UTF-8");
        String uri = Vocabulary.getInstance().getConcept( source, concept );
        if (uri == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        else
            return (Response.ok(uri).build());
    }

    @GET
    @Path( "all" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularies( @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        String[] vocabularies = Vocabulary.getInstance().getNavigableVocabularies();
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray vocabs = new JSONArray();
            for (String vocUri : vocabularies) {
                JSONObject voc = new JSONObject();
                voc.put( "uri", vocUri );
                voc.put( "restUrl", Vocabulary.getInstance().getRestUrl(vocUri));
                voc.put( "label", Vocabulary.getInstance().getVocabularyTitle(vocUri, lang, true) );
                vocabs.put(voc);
            }
            json.key( "vocabularies" ).value( vocabs );
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
    @Path( "{uri}/topConcepts" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularyTopConcepts(  @PathParam( "uri" ) String uri,
                               @DefaultValue( "false" ) @QueryParam( "showIds" ) String showIds,
                               @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {

        String[] concepts = Vocabulary.getInstance().getTopConcepts(uri, true);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray _concepts = new JSONArray();
            for (String concept : concepts)
                _concepts.put(buildJSONConcept(concept, showIds, lang));

            json.key( "concepts" ).value( _concepts );
            json.key( "label" ).value( Vocabulary.getInstance().getVocabularyTitle(uri, lang, false) );

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
    @Path( "{uri}/children" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularyConceptChildren( @PathParam( "uri" ) String uri,
                                                        @DefaultValue( "false" ) @QueryParam( "showIds" ) String showIds,
                                                        @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        String[] children = Vocabulary.getInstance().getChildren(uri);
        TripleStore tripleStore = Core.getInstance().getTripleStore();
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray _concepts = new JSONArray();
            for (String child : children)
                _concepts.put(buildJSONConcept(child, showIds, lang));

//            String label = tripleStore.getResourceLabel(uri, lang, true)[0];
//            if (Boolean.parseBoolean(showIds)) {
//                String[] spl = StringUtil.split(uri, '/');
//                String id = spl[spl.length - 1];
//                label = id + "&nbsp;&nbsp;" + label;
//            }
//            json.key( "concepts" ).value( _concepts );
//            json.key( "uri" ).value( uri );
//            json.key( "label" ).value( label );
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

    /*@GET
    @Path( "graphName" )
    @Produces( MediaType.TEXT_PLAIN )
    public Response getVocabularyConceptGraphName( @QueryParam( "uri" ) String uri ) throws Exception {
        String res = Util.getGraphName(uri);
        return Response.ok(res).build();
    }*/

    @GET
    @Path( "{uri}/subconcepts" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularySubConcepts( @PathParam( "uri" ) String uri ) throws Exception {
        String[] subs = Vocabulary.getInstance().getSubConcepts(uri);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray _concepts = new JSONArray();
            for (String c : subs)
                _concepts.put(c);

            json.key( "concepts" ).value( _concepts );
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
    @Path( "{uri}/hierarchy" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularyConceptHierarchy( @PathParam( "uri" ) String uri,
                                                 @DefaultValue( "false" ) @QueryParam( "showIds" ) String showIds,
                                                 @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        String[] concepts = Vocabulary.getInstance().getHierarchy(uri);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray _concepts = new JSONArray();
            for (String concept : concepts)
                _concepts.put(buildJSONConcept(concept, showIds, lang));

            //uri param as last element
            _concepts.put(buildJSONConcept(uri, showIds, lang));

            json.key( "concepts" ).value( _concepts );
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
    @Path( "{uri}/extendedHierarchy" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularyConceptsHierarchy( @PathParam( "uri" ) String uri,
                                                  @DefaultValue( "false" ) @QueryParam( "showIds" ) String showIds,
                                                  @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray _all = new JSONArray();

            String[] topConcepts = Vocabulary.getInstance().getTopConcepts(uri, false);
            JSONObject top = new JSONObject();
            JSONArray _concepts = new JSONArray();
            for (String concept : topConcepts)
                _concepts.put(buildJSONConcept(concept, showIds, lang));

            top.put( "concepts", _concepts );
            top.put( "label", Vocabulary.getInstance().getVocabularyTitle(uri, lang, false) );
            _all.put(top);

            String[] hierarchy = Vocabulary.getInstance().getHierarchy(uri);
            for (String concept : hierarchy) {
                String[] children = Vocabulary.getInstance().getChildren(concept);

                _concepts = new JSONArray();
                for (String child : children)
                    _concepts.put(buildJSONConcept(child, showIds, lang));

                JSONObject _element = buildJSONConcept(concept, showIds, lang);
                _element.put( "concepts", _concepts );
                _all.put(_element);
            }

            //uri param as last element
            String[] children = Vocabulary.getInstance().getChildren(uri);
            _concepts = new JSONArray();
            for (String child : children)
                _concepts.put(buildJSONConcept(child, showIds, lang));

            JSONObject _element = buildJSONConcept(uri, showIds, lang);
            _element.put( "concepts", _concepts );
            _all.put(_element);

            json.key( "all" ).value( _all );
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

    private JSONObject buildJSONConcept(String uri, String showIds, String lang) throws Exception{
        TripleStore tripleStore = Core.getInstance().getTripleStore();
        JSONObject _concept = new JSONObject();
//        String label = tripleStore.getResourceLabel(uri, lang, true)[0];
//        if (Boolean.parseBoolean(showIds)) {
//            char delimiter = '/';
//            if (uri.contains("#"))
//                delimiter = '#';
//            String[] spl = StringUtil.split(uri, delimiter);
//            String id = spl[spl.length - 1];
//            label = id + "&nbsp;&nbsp;" + label;
//        }
//        _concept.put( "uri", uri );
//        _concept.put( "restUrl", Vocabulary.getInstance().getRestUrl(uri));
//        _concept.put( "label", label );
        String[] children2 = Vocabulary.getInstance().getChildren(uri);
        if (children2.length == 0)
            _concept.put( "leaf", "true" );
        return _concept;
    }

    @GET
    @Path( "search" )
    @Produces( MediaType.APPLICATION_JSON )
    public String searchJson( @QueryParam( "q" ) String terms,
                              @DefaultValue( "false" ) @QueryParam( "showIds" ) String showIds,
                              @DefaultValue( "en" ) @QueryParam( "lang" ) String lang) throws Exception {
        if ("".equals(terms))
            return null;

        boolean showId = Boolean.parseBoolean(showIds);
//        Hashtable<String, String>[] results = Core.getInstance().getTripleStore().
//                getResultsFromGraph("getConcepts.sparql", "voc-fullTextView_" + lang,
//                        ca.licef.proeaf.core.util.Util.formatKeywords(terms));
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray concepts = new JSONArray();
//            for (int i = 0; i < results.length; i++) {
//                String uri = results[i].get("s");
//                String vocLabel = Vocabulary.getInstance().getVocabularyTitle(uri, lang, false);
//                String conceptLabel = Core.getInstance().getTripleStoreService().
//                        getResourceLabel(uri, lang, true)[0];
//                if (showId) {
//                    String[] spl = StringUtil.split(uri, '/');
//                    String id = spl[spl.length - 1];
//                    conceptLabel = id + "&nbsp;&nbsp;" + conceptLabel;
//                }
//                JSONObject concept = new JSONObject();
//                concept.put( "uri", uri );
//                concept.put( "label", conceptLabel );
//                concept.put( "vocLabel", vocLabel);
//                concepts.put(concept);
//            }
            json.key( "concepts" ).value( concepts );
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

    @Context
    private ServletContext context;
}
