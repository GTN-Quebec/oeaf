package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.vocabularies.COMETE;
import licef.tsapi.TripleStore;
import licef.tsapi.model.Triple;
import licef.tsapi.model.Tuple;
import licef.tsapi.vocabulary.RDFS;
import licef.tsapi.vocabulary.SKOS;

public class Vocabulary {

    private static Vocabulary instance;
    private VocabularyManager vocabularyManager;

    static TripleStore tripleStore = Core.getInstance().getTripleStore();
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

    public String[] getVocabularies() throws Exception {
        Triple[] triples = tripleStore.getTriplesWithPredicate(COMETE.vocUri);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getObject();
        return res;
    }

    public String[] getTopConcepts(String uri) throws Exception {
        String graph = getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithSubjectPredicate(uri, SKOS.hasTopConcept, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getObject();
        return res;
    }

    public String[] getChildren(String uri) throws Exception {
        String graph = getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithSubjectPredicate(uri, SKOS.narrower, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getObject();
        return res;
    }

    public String[] getSubConcepts(String uri) throws Exception {
        String graph = getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithSubjectPredicate(uri, SKOS.narrowerTransitive, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getObject();
        return res;
    }

    public String[] getHierarchy(String uri) throws Exception {
        String graph = getGraphName(uri);
        Triple[] triples = tripleStore.getTriplesWithPredicateObject(SKOS.narrowerTransitive, uri, false, null, graph);
        String[] res = new String[triples.length];
        for (int i = 0; i < triples.length; i++)
            res[i] = triples[i].getSubject();
        return res;
    }

    public String getLabel(String uri, String lang) throws Exception {
        String graph = getGraphName(uri);
        String[] label = tripleStore.getBetterLocalizedLiteralObject(uri, RDFS.label, lang, graph);
        if (label == null || label[ 0 ] == null || "".equals(label[ 0 ]))
            label = new String[] { uri, null } ;
        return label[0];
    }

    public String getConceptScheme(String uri) throws Exception{
        String vocUri;
        if (uri.contains("#")) //hash uri case, skos concept
            vocUri = uri.substring(0, uri.lastIndexOf('#'));
        else
            vocUri = uri.substring(0, uri.lastIndexOf('/'));

        //check existence of skos concept scheme
        String graph = getGraphNameOfVocUri(vocUri);
        if (graph == null || !Core.getInstance().getTripleStore().isResourceExists(vocUri, graph))
            vocUri = null;

        return vocUri;
    }

    public String getGraphName(String uri) throws Exception{
        String graph;
        if (uri.contains("#")) { //hash uri case, skos concept
            String vocUri = uri.substring(0, uri.lastIndexOf('#'));
            graph = getGraphNameOfVocUri(vocUri);
        }
        else {
            graph = getGraphNameOfVocUri(uri);
            if (graph != null) //uri is a skos conceptScheme
                return graph;
            else { //2nd pass with real scheme
                String vocUri = uri.substring(0, uri.lastIndexOf('/'));
                graph = getGraphNameOfVocUri(vocUri);
            }
        }

        //check existence of skos concept
        if (graph != null && !Core.getInstance().getTripleStore().isResourceExists(uri, graph))
            graph = null;
        return graph;
    }

    public String getGraphNameOfVocUri(String vocUri) throws Exception{
        //identifier may have space and uris are stored with url encoding
//        vocUri = vocUri.replace(" ", "%20");
        String graph = null;
        String query = ca.licef.proeaf.core.util.Util.getQuery("getVocGraph.sparql", vocUri);
        Tuple[] tuples = Core.getInstance().getTripleStore().sparqlSelect(query);
        if (tuples.length > 0)
            graph = tuples[0].getValue("graph").getContent();

        return graph;
    }

}