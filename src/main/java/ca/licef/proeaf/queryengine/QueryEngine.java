package ca.licef.proeaf.queryengine;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.vocabulary.Vocabulary;
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
            JSONArray facetInfos = facetedSearch(clauses, rs.getTotalRecords());
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
                String title =  tuple.getValue("title").getContent();
                if (!sigle.startsWith("_"))
                    title = "<b>" + sigle + "</b> " + title;
                entry.setTitle(title);
                String oppType = tuple.getValue("oppType").getContent();
                if (!"".equals(oppType))
                    entry.setOppType(Vocabulary.getInstance().getLabel(oppType, lang));
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
    private JSONArray facetedSearch(Hashtable<String, String> clauses, int resultCount) throws Exception {
        JSONArray facetInfos = new JSONArray();
        for (int i = 0; i < Core.getInstance().getFacetCount(); i++) {
            JSONObject facet = new JSONObject();
            String currentFacetId = "facet" + i;
            facet.put("id", currentFacetId);
            JSONArray values = new JSONArray();
            String value = Core.getInstance().getProperty(currentFacetId + ".singleCount");
            if (value == null) {
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
            }
            else {
                JSONObject facetCriteria = new JSONObject();
                if (resultCount > 0) {
                    facetCriteria.put("id", "single");
                    facetCriteria.put("count", resultCount);
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
                String clause = null;
                if (!facetCriteria.isNull("id")) {
                    String criteria = facetCriteria.getString("id");
                    if (criteria.startsWith("http://"))
                        criteria = "<" + criteria + ">";
                    else
                        criteria = "\"" + criteria + "\"";
                    clause = CoreUtil.getQuery(
                            "clause" + currentFacetId + ".sparql", criteria);
                }
                else if (!facetCriteria.isNull("from") && !facetCriteria.isNull("to")) { //between date
                    String from = facetCriteria.getString("from");
                    String to = facetCriteria.getString("to");
                    clause = CoreUtil.getQuery(
                            "clause" + currentFacetId + "between.sparql", from, to);
                }
                else if (!facetCriteria.isNull("from")) { //from date
                    String from = facetCriteria.getString("from");
                    clause = CoreUtil.getQuery(
                            "clause" + currentFacetId + "from.sparql", from);
                }
                else if (!facetCriteria.isNull("to")) { //to date
                    String to = facetCriteria.getString("to");
                    clause = CoreUtil.getQuery(
                            "clause" + currentFacetId + "to.sparql", to);
                }
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

        System.out.println("clauses = " + clauses);
        return clauses;
    }
    
    private static QueryEngine instance;
}
