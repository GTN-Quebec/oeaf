package ca.licef.proeaf.queryengine;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.ResultSet;
import ca.licef.proeaf.core.util.Util;
import licef.IOUtil;
import licef.tsapi.TripleStore;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

public class QueryEngine {

    public static QueryEngine getInstance() {
        if( instance == null ) 
            instance = new QueryEngine();
        return( instance );
    }

    public ResultSet search( String query, String lang, String outputFormat, int start, int limit, QueryCache cache ) throws Exception {
        JSONArray queryArray = new JSONArray(query);
        Object[] elements = ca.licef.proeaf.queryengine.util.Util.buildQueryElements(queryArray, cache);
        String clauses = (String)elements[0];
        int count = ((Integer)elements[1]).intValue();
        ResultSet rs = advancedSearch(clauses, count, lang, start, limit );
        String[] titleAndDesc = new String[]{};//ca.licef.proeaf.queryengine.util.Util.buildTitleAndDescription( queryArray, lang, outputFormat );
        rs.setTitle( titleAndDesc[ 0 ] );
        rs.setDescription( titleAndDesc[ 1 ] );
        return( rs );
    }

    private ResultSet advancedSearch( String clauses, int count, String lang, int start, int limit ) throws Exception {
        TripleStore tripleStore = Core.getInstance().getTripleStore();

        ResultSet rs = null;
        if( count > 0 ) {
//            Hashtable<String, String>[] results =
//                    tripleStore.getResults("getLearningObjectsAdvancedQuery.sparql", clauses, start, limit);
//            rs = buildResultSet(results, count, lang);
        }
        else
            rs = new ResultSet();

        rs.setStart( start );
        rs.setLimit( limit );         

        return rs;
    }

    private ResultSet buildResultSet( Hashtable<String, String>[] results, int count, String lang) throws Exception {
        ResultSet rs = new ResultSet();
        rs.setTotalRecords( count );
        return( rs );
    }


    
    private static QueryEngine instance;


}
