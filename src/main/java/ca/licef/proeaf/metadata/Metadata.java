package ca.licef.proeaf.metadata;

import ca.licef.proeaf.core.Core;
import licef.tsapi.model.Triple;

import java.util.Arrays;

public class Metadata {

    private static Metadata instance;

    public static Metadata getInstance() {
        if (instance == null)
            instance = new Metadata();
        return (instance);
    }

    public Triple[] getLastMinuteInfos(String uri) throws Exception {
        return Core.getInstance().getTripleStore().getTriplesWithSubjectPredicate(uri, "http://normetic.org/uri/profil_oeaf/v1.0/ns#sed3100");
    }

    public void setLastMinuteInfos(String uri, String content) throws Exception {
        Core.getInstance().getTripleStore().insertTriple(
            new Triple(uri, "http://normetic.org/uri/profil_oeaf/v1.0/ns#sed3100", content, true));
    }

    public void deleteLastMinuteInfos(String uri) throws Exception {
        Core.getInstance().getTripleStore().removeTriples(
            Arrays.asList(getLastMinuteInfos(uri)) );
    }
}
