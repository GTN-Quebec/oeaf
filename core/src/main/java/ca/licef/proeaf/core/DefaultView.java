package ca.licef.proeaf.core;

import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Triple;
import ca.licef.proeaf.core.util.Util;
import org.json.XML;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 5-Jul-2012
 */

public class DefaultView implements ResourceView {

    public String getRdf(String uri, String includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable ) throws Exception {
        Triple[] triples = getTriples( uri, includeIncomingLinks, includeRdfMetadataInfos, isHumanReadable );
        String rdf = Util.getTriplesAsRdf( triples );
        return( rdf );
    }

    public String getIncomingLinks( String uri, boolean isHumanReadable, int offset, int limit, String format ) throws Exception {
        Triple[] triples = getIncomingLinkTriples( uri, isHumanReadable, offset, limit );
        String rdf = Util.getTriplesAsRdf( triples );

        if( "rdf".equals( format ) )
            return( rdf );

        if( "json".equals( format ) )
            return( XML.toJSONObject( rdf ).toString() );

        return( null );
    }

    public String getHtml(String uri, Locale locale, ServletContext context) throws Exception {
        return "<html><body>n/a</body></html>";
    }

    public Triple[] getTriples(String uri, String includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable ) throws Exception {
        ArrayList<Triple> listTriples = new ArrayList<Triple>();
        HashMap<String,String[]> labels = new HashMap<String,String[]>();
        String rdfResUri = uri + ".rdf";

        TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();
        if( tripleStore.isResourceExists( uri ) ) {
            if( isHumanReadable ) {
                String[] objLabel = tripleStore.getResourceLabel( uri );
                labels.put( uri, objLabel );
            }

            Triple[] outgoingLinkTriples = Core.getInstance().getTripleStoreService().getTriplesWithSubject( uri );
            for( Triple triple : outgoingLinkTriples ) {
                if( isHumanReadable && !triple.isLiteral() ) {
                    String objUri = triple.getObject();
                    String[] objLabel = tripleStore.getResourceLabel( objUri );
                    if (objLabel != null && !objUri.equals( objLabel[ 0 ] ))
                        labels.put( objUri, objLabel );
                }

                listTriples.add( triple );
            }

            if( includeRdfMetadataInfos ) {
                String[] objLabel = tripleStore.getResourceLabel(uri);
                String label = objLabel[ 0 ];
                listTriples.add( new Triple( uri, Constants.FOAF_IS_PRIMARY_TOPIC_OF, rdfResUri ));
                listTriples.add( new Triple( rdfResUri, Constants.TYPE, Constants.FOAF_DOCUMENT ) );
                listTriples.add( new Triple( rdfResUri, Constants.LABEL, "RDF version of : " + label ) );
                listTriples.add( new Triple( rdfResUri, Constants.FOAF_PRIMARY_TOPIC, uri ) );
            }

            if( isHumanReadable ) {
                for( String objUri : labels.keySet() ) {
                    String[] objLabel = (String[])labels.get( objUri );
                    String label = objLabel[ 0 ];
                    String lang = objLabel[ 1 ];
                    Triple tripleObjLabel = new Triple( objUri, Constants.LABEL, label, true, lang );
                    listTriples.add( tripleObjLabel );
                }
            }

            if( "true".equals( includeIncomingLinks ) || "firstPage".equals( includeIncomingLinks ) ) {
                Triple[] incomingLinkTriples =
                        ( "firstPage".equals( includeIncomingLinks ) ?
                                getIncomingLinkTriples( uri, isHumanReadable, 0, 20 ) :
                                getIncomingLinkTriples( uri, isHumanReadable, -1, -1 ) );
                listTriples.addAll( Arrays.asList( incomingLinkTriples ) );
            }
        }

        Triple[] triples = listTriples.toArray( new Triple[ listTriples.size() ] );
        return( triples );
    }

    public Triple[] getIncomingLinkTriples(String uri, boolean isHumanReadable, int offset, int limit ) throws Exception {
        ArrayList<Triple> listTriples = new ArrayList<Triple>();
        HashMap<String,String[]> labels = new HashMap<String,String[]>();

        TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();
        Triple[] incomingLinkTriples =
                ( offset != -1 && limit != -1 ?
                        tripleStore.getTriplesWithObject( uri, false, offset, limit ) :
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
        return( triples );
    }
}
