package ca.licef.proeaf.queryengine;

import ca.licef.proeaf.core.Core;
import licef.tsapi.TripleStore;
import licef.tsapi.model.Triple;
import licef.tsapi.model.Tuple;
import org.apache.jena.atlas.json.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Hashtable;

public class QueryEngine {

    static ca.licef.proeaf.core.util.Util CoreUtil;
    TripleStore tripleStore = Core.getInstance().getTripleStore();

    public static QueryEngine getInstance() {
        if( instance == null ) 
            instance = new QueryEngine();
        return( instance );
    }

    public ResultSet search(String query, boolean isFacetInfosRequested, String lang, String outputFormat, int start, int limit) throws Exception {
        JSONArray queryArray = new JSONArray(query);
        Hashtable<String, String> clauses = buildClauses(queryArray, isFacetInfosRequested);
        ResultSet rs = advancedSearch(clauses.get("main"), lang, start, limit);
        if (rs.getSize() == 0) //1st click on no result facet, so clear facet criterias
            clauses = buildClauses(new JSONArray(), isFacetInfosRequested);
        if (isFacetInfosRequested) {
            JSONArray facetInfos = facetedSearch(clauses);
            rs.setAdditionalData("facetInfos", facetInfos);
        }
        if (queryArray.length() == 0)
            rs.setAdditionalData("isClear", true);

        return( rs );
    }

    /**
     * Search for opportunities
     * @param clauses
     * @param lang
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    private ResultSet advancedSearch(String clauses, String lang, int start, int limit) throws Exception {
        ResultSet rs = new ResultSet();
        if ("".equals(clauses)) //no result expected when no criteria
            return rs;

        String query = CoreUtil.getQuery("advancedSearchCount.sparql", clauses);
        Tuple[] tuples = tripleStore.sparqlSelect(query);
        int count = Integer.parseInt(tuples[0].getValue("count").getContent());
        if (count > 0) {
            query = CoreUtil.getQuery("advancedSearch.sparql", clauses, start, limit);
            tuples = tripleStore.sparqlSelect(query);
            for (Tuple tuple : tuples) {
                ResultEntry entry = new ResultEntry();
                entry.setId(tuple.getValue("s").getContent());
                String sigle = tuple.getValue("sigle").getContent();
                entry.setTitle("<b>" + sigle + "</b> " + tuple.getValue("title").getContent());
                entry.setLogo(tuple.getValue("providerLogo").getContent());
                rs.addEntry(entry);
            }
            rs.setTotalRecords(count);
        }

        rs.setStart( start );
        rs.setLimit( limit );
        return rs;
    }

    /**
     * Compute of facet informations related to advanced search
     * @param clauses
     * @return
     * @throws Exception
     */
    private JSONArray facetedSearch(Hashtable<String, String> clauses) throws Exception {
        JSONArray facetInfos = new JSONArray();
        for (int i = 0; i < Core.getInstance().getFacetCount(); i++) {
            JSONObject facet = new JSONObject();
            String currentFacetId = "facet" + i;
            facet.put("id", currentFacetId);
            JSONArray values = new JSONArray();
            String clauseFacet = CoreUtil.getQuery("clause" + currentFacetId + ".sparql", "?criteria");
            String otherClauses = clauses.get(currentFacetId);
            if (otherClauses == null)
                otherClauses = clauses.get("main");
            String query = CoreUtil.getQuery("facetedSearch.sparql", clauseFacet, otherClauses);
            Tuple[] tuples = tripleStore.sparqlSelect(query);
            for (Tuple tuple : tuples) {
                JSONObject facetCriteria = new JSONObject();
                int nbByCriteria = Integer.parseInt(tuple.getValue("nbByCriteria").getContent());
                if (nbByCriteria > 0) {
                    facetCriteria.put("id", tuple.getValue("criteria").getContent());
                    facetCriteria.put("count", nbByCriteria);
                    values.put(facetCriteria);
                }
            }
            facet.put("values", values);
            facetInfos.put(facet);
        }
        return facetInfos;
    }

    Hashtable<String, String> buildClauses(JSONArray queryArray, boolean isFacetInfosRequested) throws Exception {
        Hashtable<String, String> clauses = new Hashtable<String, String>();
        clauses.put("main", "");
        for (int i = 0; i < queryArray.length(); i++) {
            JSONObject facet = queryArray.getJSONObject(i);
            JSONArray values = facet.getJSONArray("values");
            String orClauses = "";
            String firstClause = null;
            String currentFacetId = facet.getString("id");
            for (int j = 0; j < values.length(); j++) {
                JSONObject facetCriteria = values.getJSONObject(j);
                String criteria = facetCriteria.getString("id");
                if (criteria.startsWith("http://"))
                    criteria = "<" + criteria + ">";
                else
                    criteria = "\"" + criteria + "\"";

                String clause = CoreUtil.getQuery(
                        "clause" + currentFacetId + ".sparql", criteria);
                if (firstClause == null)
                    firstClause = clause;
                else {
                    if (!orClauses.contains("UNION")) //braces for first clause (previous one) -AM
                        orClauses = "\n{ " + firstClause + " }";
                    orClauses += "\nUNION";
                }
                String orClause = (orClauses.endsWith("UNION"))?
                        "\n{ " + clause + " }":
                        "\n" + clause;
                orClauses += orClause;
            }
            if (!orClauses.contains("UNION"))
                orClauses += " .";

            //Cumulate union blocks
            String mainClauses = clauses.get("main");
            if (isFacetInfosRequested) {
                for (String facetKey : clauses.keySet()) {
                    if (!"main".equals(facetKey))
                        clauses.put(facetKey, clauses.get(facetKey) + orClauses);
                }
                clauses.put(currentFacetId, mainClauses);
            }
            clauses.put("main", mainClauses + orClauses);
        }
        return clauses;
    }
    
    private static QueryEngine instance;
}
