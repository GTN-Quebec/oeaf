package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.DefaultView;
import ca.licef.proeaf.core.TripleStoreService;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Triple;
import ca.licef.proeaf.core.util.Util;
import licef.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 28-Jun-2012
 */
public class VocabularyConceptView extends DefaultView {

    public String getRdf(String infos, String includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable) throws Exception {
        String[] _infos = StringUtil.split(infos, '#');
        String source = _infos[0];
        String cat = _infos[1];
        String concept = _infos[2];
        String uri = Core.getInstance().getUriPrefix() + "/voc/" + source + "/" + cat + "/" + concept;
        String graph = "voc_" + (source + "_" + cat).toLowerCase();

        ArrayList<Triple> listTriples = new ArrayList<Triple>();
        HashMap<String,String[]> labels = new HashMap<String,String[]>();

        TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();

        if( isHumanReadable ) {
            String[] objLabel = tripleStore.getResourceLabel( uri );
            labels.put( uri, objLabel );
        }

        //note: incoming and outgoing links of external vocabulary can not be humanly readable for the moment.
        //a) tripleStore.getResourceLabel method or
        //b) client call should be modified with forceVocType flag as true accordingly to objUri argument
        // -AM

        Triple[] outgoingLinkTriples = Core.getInstance().getTripleStoreService().getTriplesWithSubject( uri, graph );
        for( Triple triple : outgoingLinkTriples ) {
            if( isHumanReadable && !triple.isLiteral() ) {
                String objUri = triple.getObject();
                String[] objLabel = tripleStore.getResourceLabel( objUri );
                if (objLabel != null && !objUri.equals( objLabel[ 0 ] ))
                    labels.put( objUri, objLabel );
            }

            listTriples.add( triple );
        }

        if( "true".equals( includeIncomingLinks ) || "firstPage".equals( includeIncomingLinks ) ) {
            Triple[] incomingLinkTriples = 
                ( "firstPage".equals( includeIncomingLinks ) ? 
                    tripleStore.getTriplesWithObject( uri, false, 0, 2 ) : 
                        tripleStore.getTriplesWithObject( uri, false ) );

            for( Triple triple : incomingLinkTriples ) {
                if (isHumanReadable) {
                    String objUri = triple.getSubject();
                    String[] objLabel = tripleStore.getResourceLabel( objUri );
                    if (objLabel != null && !objUri.equals( objLabel[ 0 ] ))
                        labels.put( triple.getSubject(), objLabel );
                }
                listTriples.add( triple );
            }
        }

        if( isHumanReadable ) {
            for( String objUri : labels.keySet() ) {
                String[] objLabel = labels.get( objUri );
                String label = objLabel[ 0 ];
                String lang = objLabel[ 1 ];
                Triple tripleObjLabel = new Triple( objUri, Constants.LABEL, label, true, lang );
                listTriples.add( tripleObjLabel );
            }
        }

        Triple[] triples = listTriples.toArray( new Triple[ listTriples.size() ] );

        String rdf = Util.getTriplesAsRdf( triples );
        return( rdf );
    }
}
