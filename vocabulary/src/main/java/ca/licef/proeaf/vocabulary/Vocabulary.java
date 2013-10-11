package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.TripleStoreService;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Triple;
import ca.licef.proeaf.vocabulary.util.Util;
import org.json.JSONArray;

import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Vocabulary {

    private static Vocabulary instance;
    private VocabularyManager vocabularyManager;
    private VocabularyView vocabularyView;
    private VocabularyConceptView vocabularyConceptView;

    static TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();
    static ca.licef.proeaf.core.util.Util CoreUtil;

    public static Vocabulary getInstance() {
        if (instance == null)
            instance = new Vocabulary();
        return (instance);
    }

    public VocabularyManager getVocabularyManager() {
        if (vocabularyManager == null)
            vocabularyManager = new VocabularyManager();
        return vocabularyManager;
    }

    public VocabularyView getVocabularyView() {
        if (vocabularyView == null)
            vocabularyView = new VocabularyView();
        return vocabularyView;
    }

    public VocabularyConceptView getVocabularyConceptView() {
        if (vocabularyConceptView == null)
            vocabularyConceptView = new VocabularyConceptView();
        return vocabularyConceptView;
    }

    public String convertVdexToSkos( URL url, String vocUri ) throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put( "vocabularyUri", vocUri );
        StreamSource source = new StreamSource( url.openStream() );
        return( CoreUtil.applyXslToDocument( "convertVDEXToSKOS", source, params ) );
    }

    public String getGraphName(String source, String cat) throws Exception{
        String graph = "voc_" + (source + "_" + cat).toLowerCase();

        if (!tripleStore.isGraphExists(graph))
            graph = null;
        return graph;
    }

    public String getRestUrl(String uri) throws Exception{
        return CoreUtil.getRestUrl( Constants.TYPE_VOCABULARY ) + "/" + URLEncoder.encode(uri);
    }

    public String getConcept(String source, String cat, String concept) throws Exception{
        concept = concept.replaceAll(" ", "_");
        String graph = getGraphName(source, cat);
        String uri = null;
        if (graph != null) {
            uri = Core.getInstance().getUriPrefix() + "/voc/" + source.toLowerCase() + "/" + cat + "/" + concept;
            if (!tripleStore.isResourceExists(uri, graph))
                uri = null;
        }
        return uri;
    }

    public String getConcept(String source, String concept) throws Exception {
        String uri = null;
        String vocUri = getVocabularyUri(source);
        if (vocUri != null) {
            String graph = Util.getGraphName(vocUri);
            uri = vocUri + "/" + concept;  // '/' uri
            if (!tripleStore.isResourceExists(uri, graph)) {//check of concept existence in associated graph
                uri = vocUri + "#" + concept; // or '#' uri
                if (!tripleStore.isResourceExists(uri, graph))
                    uri = null;
            }
        }
        return uri;
    }

    public String getVocabularyUri(String source) throws Exception {
        String uri = null;
        Hashtable<String,String>[] res = tripleStore.getResults("getVocUri.sparql", source);
        if (res.length > 0)
            uri = res[0].get("vocUri");
        return uri;
    }

    public String getConceptScheme(String uri) throws Exception{
        String vocUri;
        if (uri.contains("#")) //hash uri case, skos concept
            vocUri = uri.substring(0, uri.lastIndexOf('#'));
        else {
            String graph = Util.getGraphNameOfVocUri(uri);
            if (graph != null) //uri is a skos conceptScheme)
                vocUri = uri;
            else //2nd pass with end truncation
                vocUri = uri.substring(0, uri.lastIndexOf('/'));
        }
        return vocUri;
    }

    public String getVocabularyTitle(String uri, String lang, boolean forceConceptScheme) throws Exception {
        if (!forceConceptScheme) //check and/or retrieve scheme uri first
            uri = getConceptScheme(uri);

        String[] label = tripleStore.getBestLocalizedLiteralObject(uri, Constants.LABEL, lang,
                TripleStoreService.VOC_GLOBAL_VIEW);
        if (label == null || label[ 0 ] == null || "".equals(label[ 0 ]))
            label = new String[] { uri, null } ;
        return label[0];
    }

    public String[] getNavigableVocabularies() throws Exception{
        Hashtable<String, String>[] results = tripleStore.getResults("getNavigableVocabularies.sparql");
        String[] res = new String[results.length];
        for (int i = 0; i < results.length; i++)
            res[i] = results[i].get("vocUri");
        return res;
    }

    public String[] getTopConcepts(String uri, boolean forceConceptScheme) throws Exception {
        if (!forceConceptScheme) //check and/or retrieve scheme uri first
            uri = getConceptScheme(uri);

        String graph = Util.getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithPredicateObject(Constants.SKOS_TOP_CONCEPT_OF, uri, false, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getSubject();
        return res;
    }

    /*public String[] getTopConcepts(String source, String cat) throws Exception {
        String uri = getConceptScheme(source, cat);
        return getTopConcepts(uri);
    }*/

    public String[] getChildren(String uri) throws Exception {
        String graph = Util.getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithSubjectPredicate(uri, Constants.SKOS_NARROWER, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getObject();
        return res;
    }

    public String[] getSubConcepts(String uri) throws Exception {
        String graph = Util.getGraphName(uri);
        Hashtable<String, String>[] results = tripleStore.getResultsFromGraph("getSubConcepts.sparql", graph, uri);
        String[] res = new String[results.length];
        for (int i = 0; i < results.length; i++)
            res[i] = results[i].get("c");
        return res;
    }

    public String[] getHierarchy(String uri) throws Exception {
        String graph = Util.getGraphName(uri);
        Hashtable<String, String>[] results = tripleStore.getResultsFromGraph("getConceptHierarchy.sparql", graph, uri);
        String[] res = new String[results.length];
        for (int i = 0; i < results.length; i++)
            res[i] = results[i].get("parent");
        return res;
    }

}