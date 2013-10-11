package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.DefaultView;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 28-Jun-2012
 */
public class VocabularyView extends DefaultView {

    public String getRdf(String graph, boolean includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable) throws Exception {
/*
        String fedoraId = null;
        Hashtable<String, String>[] res = Core.getInstance().getTripleStoreService().getResults("getVocContext.sparql", graph);
        if (res.length > 0)
            fedoraId = res[0].get("doId");
        FedoraService fedora = Core.getInstance().getFedoraService();
        if( fedoraId == null || !fedora.isDatastreamExists( fedoraId, Constants.DATASTREAM_SKOS ) )
            return( null );
        else
            return( fedora.getDatastream(fedoraId, Constants.DATASTREAM_SKOS) );
*/
        return "";
    }

}
