package ca.licef.proeaf.vocabulary.resource;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.util.Util;
import ca.licef.proeaf.vocabularies.COMETE;
import ca.licef.proeaf.vocabulary.Vocabulary;
import com.sun.jersey.spi.resource.Singleton;
import licef.tsapi.model.Tuple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;

@Singleton
@Path( "/vocContexts" )
public class VocContextResource {

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public String getVocabularies( @DefaultValue( "en" ) @QueryParam( "lang" ) String lang ) throws Exception {
        String query = Util.getQuery("getVocContexts.sparql");
        Tuple[] results = Core.getInstance().getTripleStore().sparqlSelect(query);
        StringWriter out = new StringWriter();
        try {
            JSONWriter json = new JSONWriter( out ).object();

            JSONArray vocCtxts = new JSONArray();
            for (int i = 0; i < results.length; i++) {
                String vocCtxtUri = results[i].getValue("s").getContent();
                String vocUri = results[i].getValue("uri").getContent();
                JSONObject voc = new JSONObject();
                voc.put( "restUrl",
                        Util.getRestUrl(COMETE.VocContext.getURI()) + "/" +
                            Util.getIdNumberValue( vocCtxtUri ));
                voc.put( "label", Vocabulary.getInstance().getLabel(vocUri, lang) );
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

}
