package ca.licef.proeaf.core;

import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Triple;
import ca.licef.proeaf.core.util.Util;
import licef.LangUtil;
import licef.StringUtil;
import org.jrdf.graph.AbstractLiteral;
import org.mulgara.connection.Connection;
import org.mulgara.connection.ConnectionFactory;
import org.mulgara.itql.TqlInterpreter;
import org.mulgara.query.Answer;
import org.mulgara.query.Query;
import org.mulgara.query.Variable;
import org.mulgara.query.operation.Command;
import org.mulgara.sparql.SparqlInterpreter;

import java.io.IOException;
import java.net.URI;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 7-Mar-2012
 */

public class TripleStoreService {

    private static final String logDir = System.getProperty( "java.io.tmpdir" );

    private String url;

    public static final String SETTINGS_GRAPH = "settings";
    public static final String MAIN_GRAPH = "main";
    public static final String MAIN_GRAPH_FULLTEXT = "main-fullText";
    public static final String MAIN_FULLTEXT_VIEW = "main-fullTextView";
    public static final String VOC_GLOBAL_VIEW = "voc-globalView";

    public static final String[] INDEX_LANGUAGES = { "en", "fr", "es" };

    public static final int PLAIN_MODEL = 0;
    public static final int FULLTEXT_MODEL = 1;
    public static final int VIEW_MODEL = 2;

    //Used for pre initialization of Mulgara and/or metamodel content
    static {
        Core.getInstance().getTripleStoreService().init();
    }

    public Connection getMulgaraConnection() {
        Connection conn = null;
        try {
            URI serverURI = new URI(getUrl());
            // Create a factory, and connect to the server
            ConnectionFactory factory = new ConnectionFactory();
            conn = factory.newConnection(serverURI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public String getUrl() {
        return( url );
    }

    public String getGraphURI(String graph) {
        return( url + "#" + graph );
    }


   /*
    * Triple Store Initialization
    */

    public void init() {
        try {
            createGraph(SETTINGS_GRAPH);
            createGraph(MAIN_GRAPH);
            createGraph(MAIN_GRAPH_FULLTEXT, FULLTEXT_MODEL);
            for( int i = 0; i < INDEX_LANGUAGES.length; i++ )
                createGraph(MAIN_GRAPH_FULLTEXT + "_" + INDEX_LANGUAGES[ i ], FULLTEXT_MODEL, INDEX_LANGUAGES[ i ]);
            for( int i = 0; i < INDEX_LANGUAGES.length; i++ )
                createFullTextView(MAIN_FULLTEXT_VIEW, MAIN_GRAPH_FULLTEXT, INDEX_LANGUAGES[ i ] );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /*
    * Graphs Management
    */

    public boolean isGraphExists(String graph) throws Exception{
        String queryString = Util.getQuery("getGraph.sparql", graph);
        Query query = new SparqlInterpreter().parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        boolean res = answer.next();
        answer.close();
        conn.close();
        return res;
    }

    /**
     * @param graph Name of the graph to create.
     * @return <tt>true</tt> if the graph has been created. Otherwise, <tt>false</tt>.
     */
    public boolean createGraph(String graph) throws Exception {
        return( createGraph(graph, PLAIN_MODEL) );
    }

    /**
     * @param graph Name of the graph to create.
     * @param modelType Type of model: PLAIN_MODEL, FULLTEXT_MODEL or VIEW_MODEL.
     * @return <tt>true</tt> if the graph has been created. Otherwise, <tt>false</tt>.
     */
    public boolean createGraph(String graph, int modelType) throws Exception {
        return( createGraph( graph, modelType, null ) );
    }

    /**
     * @param graph Name of the graph to create.
     * @param option Type of model: PLAIN_MODEL, FULLTEXT_MODEL or VIEW_MODEL.
     * @param lang Language of the graph.  Used for FULLTEXT_MODEL.
     * @return <tt>true</tt> if the graph has been created. Otherwise, <tt>false</tt>.
     */
    public boolean createGraph(String graph, int option, String lang) throws Exception {
        if (isGraphExists(graph))
            return( false );
        switch (option) {
            case FULLTEXT_MODEL: 
                String langSuffix = ( lang == null || "".equals( lang ) ? "" : "_" + lang );
                processTQLQueries("createFullTextGraph.tql", graph, langSuffix);
                return( true );
            default: /* PLAIN_MODEL */
                processTQLQuery("createGraph.tql", true, graph);
                return( true );

                //to be done : other cases ex view creation. (extreme programming...)
        }
    }

    public void createView( String viewName, String[] graphs ) throws Exception {
        if( isGraphExists( viewName ) )
            return;
        String graphClauses = "";
        String delimiter = "";
        for ( String graph : graphs ) {
            graphClauses += delimiter +
                    "<http://mulgara.org/mulgara/view> <http://mulgara.org/mulgara/view#model> <rmi://localhost/proeaf#" + graph + ">";
            delimiter = "\n";
        }
        processTQLQueries("createView.tql", viewName, graphClauses );
    }

    public void createFullTextView( String prefix, String ftGraph, String language ) throws Exception {
        String viewName = prefix + "_" + language;
        if( isGraphExists( viewName ) )
            return;
        processTQLQueries("createFullTextView.tql", viewName, ftGraph, language );
    }

    public void loadRDFContent(String source, String graph) throws Exception {
        String status = processTQLQuery("loadRDFIntoGraph.tql", false, source, graph);
        System.out.println("mulgara : " + status + " triples inserted into #" + graph);
    }

    public void dropGraph(String graph) throws Exception {
        if (!isGraphExists(graph))
            return;
        processTQLQuery("dropGraph.tql", true, graph);
    }

    public void dropAllGraphsStartWith(String prefix) throws Exception {
        String queryString = Util.getQuery("getAllGraphs.sparql");
        Query query = new SparqlInterpreter().parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            String s = answer.getObject(0).toString();
            if (s.startsWith("rmi://") && !s.endsWith("#")) {
                String[] array = StringUtil.split(s, '#');
                String graph = array[1];
                if (graph.startsWith(prefix))
                    dropGraph(graph);
            }
        }
        answer.close();
        conn.close();
    }

   /*
    * Rules Management
    */

    public void applyRules(String rulesFile, String graph) throws Exception {
        processTQLQuery("applyRules.tql", true, rulesFile, graph);
    }

    /*
     * Generic TQL queries
     */
    public String processTQLQuery(String queryName, boolean displayStatus, Object... params) throws Exception {
        String queryString = Util.getQuery(queryName, params);
        Command command = new TqlInterpreter().parseCommand(queryString);
        Connection conn = getMulgaraConnection();
        String status = conn.execute(command);
        if (displayStatus)
            System.out.println("mulgara : " + status);
        conn.close();
        return status;
    }
    
    public void processTQLQueries(String queryName, Object... params) throws Exception {
        String queryString = Util.getQuery( queryName, params );
        List<Command> commands = new TqlInterpreter().parseCommands( queryString );
        Connection conn = getMulgaraConnection();
        for( Iterator it = commands.iterator(); it.hasNext(); ) {
            Command command = (Command)it.next();
            String status = conn.execute( command );
            System.out.println( "mulgara : " + status );
        }
        conn.close();
    }


   /*
    * Triples Management
    */

    public String getQueryTriple(String subject, String predicate, String object, boolean isLiteral, String lang) {
        String res = "";
        boolean isLang = (lang != null && !"".equals(lang));
        boolean isDate = isLiteral && Constants.DATE_PREDICATES.contains(predicate);
        if (isDate)
            object = "\'" + object + "\'^^" + Constants.XSD_DATE_TYPE;
        else if (isLiteral)
            object = "\'" + Util.encodeSingleQuote( object ) + "\'" +(isLang?"@" + Util.formatLanguage(lang):"");
        else
            object = "<" + object + ">";

        res += "<" + subject + "> " + "<" + predicate + "> " + object;

        return res;
    }


    /**
     * Triple adding
     */
    public void addTriple(Triple triple) throws Exception {
        addTriple(triple, MAIN_GRAPH);
    }

    public void addTriple(Triple triple, String graph) throws Exception {
        boolean isLiteral = triple.isLiteral();
        if (!isLiteral) //in case of ommit isLiteral value set
            isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
        addTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, triple.getLanguage(), graph);
    }

    public void addTriple(String subject, String predicate, String object) throws Exception {
        addTriple(subject, predicate, object, null);
    }

    public void addTriple(String subject, String predicate, String object, String lang) throws Exception {
        addTriple(subject, predicate, object, lang, MAIN_GRAPH);
    }

    public void addTriple(String subject, String predicate, String object, String lang, String graph) throws Exception {
        addTriple(subject, predicate, object, Constants.LITERAL_PREDICATES.contains(predicate), lang,  graph);
    }

    public void addTriple(String subject, String predicate, String object, boolean isLiteral, String lang) throws Exception {
        addTriple(subject, predicate, object, isLiteral, lang, MAIN_GRAPH);
    }

    public void addTriple(String subject, String predicate, String object, boolean isLiteral, String lang, String graph) throws Exception {
        lang = LangUtil.convertLangToISO2( lang );
        String queryTriple = getQueryTriple(subject, predicate, object, isLiteral, lang);

        boolean isLang = lang != null && !"".equals(lang);
        addTriplesEff(queryTriple, graph);

        //fulltext case
        if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(predicate) && object != null && !"".equals(object.trim()) ) {
            if( isLang ) {
                String indexLang = getAssocIndexLanguage( lang );
                if( indexLang != null )
                    addTriplesEff(queryTriple, MAIN_GRAPH_FULLTEXT + "_" + indexLang );
            }
            else
                addTriplesEff(queryTriple, MAIN_GRAPH_FULLTEXT );
        }
    }

    public void addTriples(Triple[] triples) throws Exception {
        addTriples(triples, MAIN_GRAPH);
    }

    public void addTriples(Collection<Triple> triples) throws Exception {
        addTriples(triples, MAIN_GRAPH);
    }

    public void addTriples(Triple[] triples, String graph) throws Exception {
        addTriples(Arrays.asList(triples), graph);
    }

    public void addTriples(Collection<Triple> triples, String graph) throws Exception {
        if (triples.size() == 0)
            return;

        //isolate literals triples to be indexed
        ArrayList<Triple> ftTriples = new ArrayList<Triple>();

        //triples to be inserted in param graph
        String queryTriples = "";
        for (Triple triple : triples) {
            boolean isLiteral = triple.isLiteral();
            if (!isLiteral) //in case of ommit isLiteral value set
                isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
            String queryTriple = getQueryTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, triple.getLanguage());
            queryTriples += queryTriple + " ";
            if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(triple.getPredicate()))
                ftTriples.add(triple);
        }
        //insertions
        addTriplesEff(queryTriples, graph);

        //ft adding
        addFTTriples(ftTriples, MAIN_GRAPH_FULLTEXT);
    }

    public void addFTTriples(Triple[] triples, String ftgraph) throws Exception {
        addFTTriples(Arrays.asList(triples), ftgraph);
    }

    public void addFTTriples(Collection<Triple> triples, String ftgraph) throws Exception {
        if (triples.size() == 0)
            return;

        //triples to be inserted in fullText graph
        String queryTriplesFT = "";
        //triples to be inserted in fullText_bylang graph
        Hashtable<String, String> queryTriplesFTLang = new Hashtable<String, String>();

        for (Triple triple : triples) {
            // Skip triples with empty values.
            if( triple.isEmptyObject() ) 
                continue;

            boolean isLiteral = triple.isLiteral();
            if (!isLiteral) //in case of ommit isLiteral value set
                isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
            String lang = triple.getLanguage();
            boolean isLang = lang != null && !"".equals(lang);
            String queryTriple = getQueryTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, lang);
            //check of literal triple but should be unnecessary -AM
            if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(triple.getPredicate())) {
                if( isLang ) {
                    String indexLang = getAssocIndexLanguage( lang );
                    if( indexLang != null ) {
                        String qtftl = queryTriplesFTLang.get(indexLang);
                        if (qtftl != null)
                            qtftl += queryTriple + " ";
                        else
                            qtftl = queryTriple + " ";
                        queryTriplesFTLang.put(indexLang, qtftl);
                    }
                }
                else
                    queryTriplesFT += queryTriple + " ";
            }
        }
        //insertions
        addTriplesEff(queryTriplesFT, ftgraph);
        for (Enumeration en = queryTriplesFTLang.keys(); en.hasMoreElements();) {
            String langEff = (String)en.nextElement();
            addTriplesEff(queryTriplesFTLang.get(langEff), ftgraph + "_" + langEff);
        }
    }

    private void addTriplesEff(String queryTriples, String graph) throws Exception {
        if (!"".equals(queryTriples))
            processTQLQuery("insertTriplesIntoGraph.tql", false, queryTriples, graph);
    }

    
    /**
     * Triple deletion
     */
    public void deleteTriple(Triple triple) throws Exception {
        deleteTriple(triple, MAIN_GRAPH);
    }

    public void deleteTriple(Triple triple, String graph) throws Exception {
        boolean isLiteral = triple.isLiteral();
        if (!isLiteral) //in case of ommit isLiteral value set
            isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
        deleteTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, triple.getLanguage() , graph);
    }

    public void deleteTriple(String subject, String predicate, String object) throws Exception {
        deleteTriple(subject, predicate, object, null);
    }

    public void deleteTriple(String subject, String predicate, String object, String lang) throws Exception {
        deleteTriple(subject, predicate, object, lang, MAIN_GRAPH);
    }

    public void deleteTriple(String subject, String predicate, String object, String lang, String graph) throws Exception {
        deleteTriple(subject, predicate, object, Constants.LITERAL_PREDICATES.contains(predicate), lang, graph);
    }

    public void deleteTriple(String subject, String predicate, String object, String lang, boolean isLiteral) throws Exception {
        deleteTriple(subject, predicate, object, isLiteral, lang, MAIN_GRAPH);
    }

    public void deleteTriple(String subject, String predicate, String object, boolean isLiteral, String lang, String graph) throws Exception {
        lang = LangUtil.convertLangToISO2( lang );
        String queryTriple = getQueryTriple(subject, predicate, object, isLiteral, lang);

        boolean isLang = lang != null && !"".equals(lang);
        deleteTriplesEff(queryTriple, graph);

        //fulltext case
        if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(predicate)) {
            if( isLang ) {
                String indexLang = getAssocIndexLanguage( lang );
                if( indexLang != null )
                    deleteTriplesEff(queryTriple, MAIN_GRAPH_FULLTEXT + "_" + indexLang);
            }
            else
                deleteTriplesEff(queryTriple, MAIN_GRAPH_FULLTEXT );
        }
    }

    public void deleteTriples(Triple[] triples) throws Exception {
        deleteTriples(triples, MAIN_GRAPH);
    }

    public void deleteTriples(Collection<Triple> triples) throws Exception {
        deleteTriples(triples, MAIN_GRAPH);
    }

    public void deleteTriples(Triple[] triples, String graph) throws Exception {
        deleteTriples(Arrays.asList(triples), graph);
    }

    public void deleteTriples(Collection<Triple> triples, String graph) throws Exception {
        if (triples.size() == 0)
            return;

        //isolate literals triples to be indexed
        ArrayList<Triple> ftTriples = new ArrayList<Triple>();

        //triples to be deleted in param graph
        String queryTriples = "";
        for (Triple triple : triples) {
            boolean isLiteral = triple.isLiteral();
            if (!isLiteral) //in case of ommit isLiteral value set
                isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
            String queryTriple = getQueryTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, triple.getLanguage());
            queryTriples += queryTriple + " ";
            if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(triple.getPredicate()))
                ftTriples.add(triple);
        }
        //deletions
        deleteTriplesEff(queryTriples, graph);
        //ft deletion
        deleteFTTriples(ftTriples, MAIN_GRAPH_FULLTEXT);
    }

    public void deleteFTTriples(Triple[] triples, String ftgraph) throws Exception {
        deleteFTTriples(Arrays.asList(triples), ftgraph);
    }

    public void deleteFTTriples(Collection<Triple> triples, String ftgraph) throws Exception {
        if (triples.size() == 0)
            return;

        //triples to be deleted in main-fullText
        String queryTriplesFT = "";
        //triples to be deleted in main-fullText_bylang
        Hashtable<String, String> queryTriplesFTLang = new Hashtable<String, String>();
        for (Triple triple : triples) {
            boolean isLiteral = triple.isLiteral();
            if (!isLiteral) //in case of ommit isLiteral value set
                isLiteral = Constants.LITERAL_PREDICATES.contains(triple.getPredicate());
            String lang = triple.getLanguage();
            boolean isLang = lang != null && !"".equals(lang);
            String queryTriple = getQueryTriple(triple.getSubject(), triple.getPredicate(), triple.getObject(), isLiteral, lang);
            //check of literal triple but should be unnecessary -AM
            if (isLiteral && Constants.FULL_TEXT_PREDICATES.contains(triple.getPredicate())) {
                if( isLang ) {
                    String indexLang = getAssocIndexLanguage( lang );
                    if( indexLang != null ) {
                        String qtftl = queryTriplesFTLang.get(indexLang);
                        if (qtftl != null)
                            qtftl += queryTriple + " ";
                        else
                            qtftl = queryTriple + " ";
                        queryTriplesFTLang.put(indexLang, qtftl);
                    }
                }
                else
                    queryTriplesFT += queryTriple + " ";
            }
        }
        //deletions
        deleteTriplesEff(queryTriplesFT, ftgraph);
        for (Enumeration en = queryTriplesFTLang.keys(); en.hasMoreElements();) {
            String langEff = (String)en.nextElement();
            deleteTriplesEff(queryTriplesFTLang.get(langEff), ftgraph + "_" + langEff);
        }
    }

    private void deleteTriplesEff(String queryTriples, String graph) throws Exception {
        if (!"".equals(queryTriples))
            processTQLQuery("deleteTriplesFromGraph.tql", false, queryTriples, graph);
    }




    /**
     * Triple retrieving
     */

    public Triple[] getTriplesWithSubject(String subject) throws Exception {
        return getTriplesWithSubject(subject, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubject(String subject, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String queryString = Util.getQuery( "getTriplesWithSubject.sparql", subject, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            String predicate = answer.getObject(0).toString();
            Object object = answer.getObject(1);
            boolean isLiteral = object instanceof AbstractLiteral;
            String obj = object.toString();
            String lang = null;
            if (isLiteral) {
                if (Util.isDate(obj)) {
                    obj = Util.manageDateString(obj);
                }
                else {
                    String[] vals = Util.manageLocalizedString(obj);
                    obj = vals[0];
                    lang = vals[1];
                }
            }
            Triple triple = new Triple( subject, predicate, obj, isLiteral, lang );
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public int getTriplesWithSubjectCount(String subject, int offset, int limit) throws Exception {
        return getTriplesWithSubjectCount(subject, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubject(String subject, String graph) throws Exception {
        return( getTriplesWithSubject( subject, -1, -1, graph ) );
    }

    public int getTriplesWithSubjectCount(String subject) throws Exception {
        return getTriplesWithSubjectCount(subject, -1, -1, MAIN_GRAPH);
    }

    public int getTriplesWithSubjectCount(String subject, int offset, int limit, String graph) throws Exception {
        return getResultsCountFromGraph("getTriplesWithSubject.sparql", graph, subject, buildRange( offset, limit ) );
    }

    public int getTriplesWithSubjectCount(String subject, String graph) throws Exception {
        return( getTriplesWithSubjectCount( subject, -1, -1, graph ) );
    }

    public Triple[] getTriplesWithSubjectPredicate(String subject, String predicate, int offset, int limit) throws Exception {
        return getTriplesWithSubjectPredicate(subject, predicate, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubjectPredicate(String subject, String predicate) throws Exception {
        return getTriplesWithSubjectPredicate(subject, predicate, -1, -1, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubjectPredicate(String subject, String predicate, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String queryString = Util.getQuery( "getTriplesWithSubjectPredicate.sparql", subject, predicate, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            Object object = answer.getObject(0);
            boolean isLiteral = object instanceof AbstractLiteral;
            String obj = object.toString();
            String lang = null;
            if (isLiteral) {
                if (Util.isDate(obj)) {
                    obj = Util.manageDateString(obj);
                }
                else {
                    String[] vals = Util.manageLocalizedString(obj);
                    obj = vals[0];
                    lang = vals[1];
                }
            }
            Triple triple = new Triple( subject, predicate, obj, isLiteral, lang);
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public Triple[] getTriplesWithSubjectPredicate(String subject, String predicate, String graph) throws Exception {
        return( getTriplesWithSubjectPredicate( subject, predicate, -1, -1, graph ) );
    }

    public Triple[] getTriplesWithSubjectPredicateStartsWith(String subject, String predicatePrefix, int offset, int limit) throws Exception {
        return getTriplesWithSubjectPredicateStartsWith(subject, predicatePrefix, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubjectPredicateStartsWith(String subject, String predicatePrefix) throws Exception {
        return getTriplesWithSubjectPredicateStartsWith(subject, predicatePrefix, -1, -1, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithSubjectPredicateStartsWith(String subject, String predicatePrefix, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String queryString = Util.getQuery( "getTriplesWithSubjectPredicateStartsWith.sparql", subject, predicatePrefix, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            String predicate = answer.getObject(0).toString();
            Object object = answer.getObject(1);
            boolean isLiteral = object instanceof AbstractLiteral;
            String obj = object.toString();
            String lang = null;
            if (isLiteral) {
                if (Util.isDate(obj)) {
                    obj = Util.manageDateString(obj);
                }
                else {
                    String[] vals = Util.manageLocalizedString(obj);
                    obj = vals[0];
                    lang = vals[1];
                }
            }
            Triple triple = new Triple( subject, predicate, obj, isLiteral, lang );
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public Triple[] getTriplesWithSubjectPredicateStartsWith(String subject, String predicatePrefix, String graph) throws Exception {
        return( getTriplesWithSubjectPredicateStartsWith( subject, predicatePrefix, -1, -1, graph ) );
    }

    public Triple[] getTriplesWithPredicate(String predicate, int offset, int limit) throws Exception {
        return getTriplesWithPredicate(predicate, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithPredicate(String predicate) throws Exception {
        return getTriplesWithPredicate(predicate, -1, -1, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithPredicate(String predicate, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String queryString = Util.getQuery( "getTriplesWithPredicate.sparql", predicate, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            Object object = answer.getObject(1);
            boolean isLiteral = object instanceof AbstractLiteral;
            String obj = object.toString();
            String lang = null;
            if (isLiteral) {
                if (Util.isDate(obj)) {
                    obj = Util.manageDateString(obj);
                }
                else {
                    String[] vals = Util.manageLocalizedString(obj);
                    obj = vals[0];
                    lang = vals[1];
                }
            }
            Triple triple = new Triple( answer.getObject(0).toString(), predicate, obj, isLiteral, lang);
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public Triple[] getTriplesWithPredicate(String predicate, String graph) throws Exception {
        return( getTriplesWithPredicate( predicate, -1, -1, graph ) );
    }

    public Triple[] getTriplesWithPredicateObject(String predicate, String object, int offset, int limit, boolean isLiteral) throws Exception {
        return getTriplesWithPredicateObject(predicate, object, isLiteral, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithPredicateObject(String predicate, String object, boolean isLiteral) throws Exception {
        return getTriplesWithPredicateObject(predicate, object, isLiteral, -1, -1, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithPredicateObject(String predicate, String object, boolean isLiteral, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String object2 = isLiteral?"\"" + object + "\"":"<" + object + ">";
        String queryString = Util.getQuery( "getTriplesWithPredicateObject.sparql", predicate, object2, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            Triple triple = new Triple( answer.getObject(0).toString(), predicate, object, isLiteral);
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public Triple[] getTriplesWithPredicateObject(String predicate, String object, boolean isLiteral, String graph) throws Exception {
        return( getTriplesWithPredicateObject( predicate, object, isLiteral, -1, -1, graph ) );
    }

    public Triple[] getTriplesWithObject(String object, boolean isLiteral, int offset, int limit) throws Exception {
        return getTriplesWithObject(object, isLiteral, offset, limit, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithObject(String object, boolean isLiteral) throws Exception {
        return getTriplesWithObject(object, isLiteral, -1, -1, MAIN_GRAPH);
    }

    public Triple[] getTriplesWithObject(String object, boolean isLiteral, int offset, int limit, String graph) throws Exception {
        ArrayList<Triple> triples = new ArrayList<Triple>();
        String object2 = isLiteral?"\"" + object + "\"":"<" + object + ">";
        String queryString = Util.getQuery( "getTriplesWithObject.sparql", object2, buildRange( offset, limit ) );
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            String subject = answer.getObject(0).toString();
            String predicate = answer.getObject(1).toString();
            Triple triple = new Triple( subject, predicate, object, isLiteral );
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

    public Triple[] getTriplesWithObject(String object, boolean isLiteral, String graph) throws Exception {
        return( getTriplesWithObject( object, isLiteral, -1, -1, graph ) );
    }

    public Triple[] getTriples(String queryName, Object... params) throws Exception {
        return getTriplesFromGraph(queryName, MAIN_GRAPH, params);
    }

    public  Triple[] getTriplesFromGraph( String queryName, String graph, Object... params ) throws Exception {
        return getQueryTriplesFromGraph(Util.getQuery( queryName, params ), graph);
    }

    public Triple[] getQueryTriplesFromGraph(String queryString) throws Exception {
        return getQueryTriplesFromGraph(queryString, MAIN_GRAPH);
    }

    public Triple[] getQueryTriplesFromGraph(String queryString, String graph) throws Exception {
        try {
            licef.IOUtil.writeStringToFile(queryString, new java.io.File(logDir + "/queries.log"), true);
        } catch (IOException e) {
            System.out.println("e = " + e);
        }

        ArrayList<Triple> triples = new ArrayList<Triple>();
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            String subject = answer.getObject(0).toString();
            String predicate = answer.getObject(1).toString();
            Object object = answer.getObject(2);
            boolean isLiteral = object instanceof AbstractLiteral;
            String obj = object.toString();
            String lang = null;
            if (isLiteral) {
                if (Util.isDate(obj)) {
                    obj = Util.manageDateString(obj);
                }
                else {
                    String[] vals = Util.manageLocalizedString(obj);
                    obj = vals[0];
                    lang = vals[1];
                }
            }
            Triple triple = new Triple( subject, predicate, obj, isLiteral, lang );
            triples.add( triple );
        }
        answer.close();
        conn.close();
        return( triples.toArray(new Triple[triples.size()]) );
    }

   /*
    * Tuples Management with SPARQL
    */

    public Hashtable<String, String>[] getResults( String queryName, Object... params ) throws Exception {
        return getResultsFromGraph(queryName, MAIN_GRAPH, params);
    }

    public Hashtable<String, String>[] getResultsFromGraph( String queryName, String graph, Object... params ) throws Exception {
        return getQueryResultsFromGraph(Util.getQuery( queryName, params ), graph);
    }

    public Hashtable<String, String>[] getQueryResults( String queryString ) throws Exception {
        return getQueryResultsFromGraph(queryString, MAIN_GRAPH);
    }

    public Hashtable<String, String>[] getQueryResultsFromGraph( String queryString, String graph ) throws Exception {
        try {
            licef.IOUtil.writeStringToFile(queryString, new java.io.File(logDir + "/queries.log"), true);
        } catch (IOException e) {
            System.out.println("e = " + e);
        }

        ArrayList<Hashtable<String, String>> list = new ArrayList<Hashtable<String, String>>();
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        Variable[] vars = answer.getVariables();
        while (answer.next()) {
            Hashtable<String, String> t = new Hashtable<String, String>();
            for (Variable var : vars) {
                Object value = answer.getObject(var.getName());
                String val = (value == null)?"":value.toString();
                t.put(var.getName(), val);
            }
            list.add(t);
        }
        answer.close();
        conn.close();

        Hashtable<String, String>[] results = new Hashtable[list.size()];
        for (int i = 0; i < list.size(); i++)
            results[i] = list.get(i);
        return results;
    }

    public int getResultsCount(String queryName, Object... params) throws Exception {
        return getResultsCountFromGraph(queryName, MAIN_GRAPH, params);
    }

    public int getResultsCountFromGraph( String queryName, String graph, Object... params ) throws Exception {
        return getQueryResultsCountFromGraph(Util.getQuery( queryName, params ), graph);
    }

    public int getQueryResultsCount( String queryString ) throws Exception {
        return getQueryResultsCountFromGraph(queryString, MAIN_GRAPH);
    }

    public int getQueryResultsCountFromGraph( String queryString, String graph ) throws Exception {
        try {
            licef.IOUtil.writeStringToFile(queryString, new java.io.File(logDir + "/countQueries.log"), true);
        } catch (IOException e) {
            System.out.println("e = " + e);
        }

        int count = 0;
        SparqlInterpreter si = new SparqlInterpreter();
        si.setDefaultGraphUri(getGraphURI(graph));
        Query query = si.parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        while (answer.next()) {
            count++;
        }
        answer.close();
        conn.close();
        return count;
    }

   /*
    * Tuples Management with TQL
    * WARNING: Don't put alias into tql query file. -AM
    */

    public Hashtable<String, String>[] getResultsWithTQL(String queryName, String unificationKey, Object... params) throws Exception {
        ArrayList<Hashtable<String, String>> list = new ArrayList<Hashtable<String, String>>();
        String queryString = Util.getQuery( queryName, params );
        Query query = new TqlInterpreter().parseQuery(queryString);
        Connection conn = getMulgaraConnection();
        Answer answer = conn.execute(query);
        answer.beforeFirst();
        Variable[] vars = answer.getVariables();
        String currentUnifKey = null;
        Hashtable<String, String> t = null;
        while (answer.next()) {
            for (Variable var : vars) {
                String key = answer.getObject(unificationKey).toString();
                if (!key.equals(currentUnifKey)) {
                    t = new Hashtable<String, String>();
                    list.add(t);
                    currentUnifKey = key;
                }
                String prev = t.get(var.getName());
                if (prev == null || "".equals(prev)) {
                    Object value = answer.getObject(var.getName());
                    String val = (value == null)?"":value.toString();
                    t.put(var.getName(), val);
                    System.out.println(var.getName()+ " = " + val);
                }
            }
        }
        answer.close();
        conn.close();

        Hashtable<String, String>[] results = new Hashtable[list.size()];
        for (int i = 0; i < list.size(); i++)
            results[i] = list.get(i);
        return results;
    }


    /**
     * Retrieve the best literal with following ranking :
     * 1 - With exactly same language
     * 2 - With first similar language (ex: fr-CA for a fr request)
     * 3 - unlocalized literal when no matching language
     * 4 - default case : first found result
     * @returns Array of 2 Strings: the first is the best literal, the second is its language.
     */
    public String[] getBestLocalizedLiteralObject(String uri, String predicate, String lang) throws Exception {
        return getBestLocalizedLiteralObject(uri, predicate, lang, MAIN_GRAPH);
    }

    public String[] getBestLocalizedLiteralObject(String uri, String predicate, String lang, String graph) throws Exception {
        lang = LangUtil.convertLangToISO2( lang );
        Triple[] triples = getTriplesWithSubjectPredicate(uri, predicate, graph);
        if (lang == null)
            lang = "###"; //to force unlocalized choice
        String[] res = null;
        boolean foundWithSimilarLanguage = false;
        boolean foundWithoutLanguage = false;
        for (Triple t: triples) {
            if (t.getLanguage() != null && t.getLanguage().toLowerCase().equals(lang.toLowerCase())) {
                res = new String[] { t.getObject(), t.getLanguage() };
                break;
            }
            if (t.getLanguage() != null &&
                    ( (t.getLanguage().toLowerCase().startsWith(lang.toLowerCase())) ||
                      (lang.startsWith(t.getLanguage().toLowerCase())) ) &&
                    !foundWithSimilarLanguage) {
                res = new String[] { t.getObject(), t.getLanguage() };
                foundWithSimilarLanguage = true;
            }
            if (t.getLanguage() == null && !foundWithSimilarLanguage && !foundWithoutLanguage) {
                res = new String[] { t.getObject(), t.getLanguage() };
                foundWithoutLanguage = true;
            }
            if (res == null)
                res = new String[] { t.getObject(), t.getLanguage() };
        }
        return res;
   }

    /*
    * Resources Management
    */

    public boolean isResourceExists(String uri) throws Exception {
        return isResourceExists(uri, MAIN_GRAPH);
    }

    public boolean isResourceExists(String uri, String graph) throws Exception {
        int count = getTriplesWithSubjectCount(uri, graph);
        return count > 0;
    }

    public String[] getResourceLabel(String uri) throws Exception {
        return getResourceLabel(uri, null, false);
    }

    public String[] getResourceLabel(String uri, String lang) throws Exception {
        return getResourceLabel(uri, lang, false);
    }

    /**
     *
     * @param uri
     * @param lang
     * @param forceVocType used for external voc uris
     * @return
     * @throws Exception
     */
    public String[] getResourceLabel(String uri, String lang, boolean forceVocType) throws Exception {
        lang = LangUtil.convertLangToISO2( lang );
        String predicate = Constants.LABEL; //rdfs:label default case
        String graph = TripleStoreService.MAIN_GRAPH;

        //vocabulary concept case
/*        String type = Util.getURIType(uri);
        if (type == null && !forceVocType)
            return( new String[] { uri, null } );

        if (forceVocType || Constants.TYPE_VOCABULARY.equals( type ) || Constants.TYPE_VOCABULARY_CONCEPT.equals( type ) ) {
            graph = VOC_GLOBAL_VIEW;
            predicate = Constants.SKOS_LABEL;
        }
        else if (type.equals(Constants.TYPE_LEARNING_OBJECT))
            predicate = Constants.METAMODEL_TITLE;
        else if (type.equals(Constants.TYPE_PERSON))
            predicate = Constants.METAMODEL_IDENTITY_NAME;
        else if (type.equals(Constants.TYPE_ORGANIZATION))
            predicate = Constants.METAMODEL_IDENTITY_NAME;
*/
        String[] label = getBestLocalizedLiteralObject(uri, predicate, lang, graph);
        if (label == null || label[ 0 ] == null || "".equals(label[ 0 ]))
            return( new String[] { uri, null } );
        return label;
    }

    /**
     * Relationship substitutions of object1 by object2
     * @param object1 old ref object
     * @param object2 new target object
     */
    public void redirectObjectResource(String object1, String object2) throws Exception {
        ArrayList<Triple> newTriples = new ArrayList<Triple>();

        Triple[] triples = getTriplesWithObject(object1, false);
        //new affectations
        for (Triple triple : triples)
            newTriples.add(new Triple(triple.getSubject(), triple.getPredicate(), object2));
        addTriples(newTriples);

        //remove of links
        deleteTriples(triples);
    }

    public void deleteResource(String uri) throws Exception {
        deleteResource(uri, MAIN_GRAPH);
    }

    public void deleteResource(String uri, String graph) throws Exception {
        Triple[] triples = getTriplesWithObject(uri, false, graph);
        deleteTriples(triples, graph);
        triples = getTriplesWithSubject(uri, graph);
        deleteTriples(triples, graph);
    }

    public void replacePredicate(String currentPredicate, String newPredicate) throws Exception {
        replacePredicate(currentPredicate, newPredicate, MAIN_GRAPH);
    }

    public void replacePredicate(String currentPredicate, String newPredicate, String graph) throws Exception {
        Triple[] triples = getTriplesWithPredicate(currentPredicate, graph);
        deleteTriples(triples, graph);
        for (Triple triple : triples)
            triple.setPredicate(newPredicate);
        addTriples(triples, graph);
    }

    public String getAssocIndexLanguage( String langCode ) {
        String indexLang = null;

        // Consider only the first part of the language (e.g.: fr for fr_CA).
        String lang = langCode.split( "[-_]" )[ 0 ];

        for( int i = 0; i < INDEX_LANGUAGES.length; i++ ) {
            if( INDEX_LANGUAGES[ i ].equals( lang ) )
                return( lang );
        }
        return( null );
    }

    private String buildRange( int offset, int limit ) {
        StringBuilder str = new StringBuilder();
        if( offset != -1 )
            str.append( "OFFSET " ).append( offset ).append( " " );
        if( limit != -1 )
            str.append( "LIMIT " ).append( limit );
        return( str.toString() );
    }

}
