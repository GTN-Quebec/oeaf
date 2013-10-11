package ca.licef.proeaf.queryengine.util;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.TripleStoreService;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.queryengine.QueryCache;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import licef.DateUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-01-16
 */
public class Util {

    static TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();
    static ca.licef.proeaf.core.util.Util CoreUtil;

    /**
     * @return clause element and pre compute size
     */
    public static Object[] buildQueryElements(JSONArray queryArray, QueryCache cache) throws Exception {
        String clauses = "";
        boolean waitForOperator = false;

        String orClauses = "";
        String firstClause = null;

        for (int i = 0; i < queryArray.length(); i++) {
            JSONObject obj = queryArray.getJSONObject(i);
            if (waitForOperator) {
                String op = obj.getString("op");
                if ("AND".equals(op)) {
                    clauses += orClauses;
                    orClauses = "";
                    firstClause = null;
                }
                waitForOperator = !waitForOperator;
            }
            else {


                waitForOperator = !waitForOperator;
            }
        }
        //last cond
        clauses += orClauses;

        int count = 0;//tripleStore.getResultsCount("getLearningObjectsAdvancedQueryForCount.sparql", clauses);

        return new Object[]{clauses, count};
    }
}
