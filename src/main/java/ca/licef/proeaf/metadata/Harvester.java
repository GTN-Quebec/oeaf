package ca.licef.proeaf.metadata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ca.licef.proeaf.core.Core;
import licef.IOUtil;
import licef.tsapi.TripleStore;
import licef.tsapi.model.Triple;

public class Harvester implements Runnable {

    public Harvester( String name, String url, String type ) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.isRunning = false;
    }

    // This method should be more robust. - FB
    public Set<String> extractOEAFLinks( BufferedReader reader ) throws Exception {
        Set<String> links = new HashSet<String>();
        String line = null;
        while( ( line = reader.readLine() ) != null ) {
            int indexOfOEAFMark = line.indexOf( "OEAF_" );
            if( indexOfOEAFMark != -1 ) {
                int indexOfClosingQuote = line.indexOf( "\"", indexOfOEAFMark + "OEAF_".length() + 1 );
                if( indexOfClosingQuote != -1 ) {
                    String linkType = line.substring( indexOfOEAFMark + "OEAF_".length(), indexOfClosingQuote );
                    int indexOfHref = line.indexOf( "href=\"" );
                    if( indexOfHref != -1 ) {
                        indexOfClosingQuote = line.indexOf( "\"", indexOfHref + "href=\"".length() + 1 );
                        if( indexOfClosingQuote != -1 ) {
                            String link = line.substring( indexOfHref + "href=\"".length(), indexOfClosingQuote );
                            links.add( linkType + "|" + link );
                        }
                    }
                    else {
                        int indexOfSrc = line.indexOf( "src=\"" );
                        if( indexOfSrc != -1 ) {
                            indexOfClosingQuote = line.indexOf( "\"", indexOfSrc + "src=\"".length() + 1 );
                            if( indexOfClosingQuote != -1 ) {
                                String link = line.substring( indexOfSrc + "src=\"".length(), indexOfClosingQuote );
                                links.add( linkType + "|" + link );
                            }
                        }
                    }
                }
            }
        }
        return( links );
    }

    public void harvestFile( String url, String linkType, String... graph ) throws Exception {
        System.out.println( "Harvesting linkType="+ linkType + " url=" + url );

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet get = new HttpGet( url );
       
        int attempt = 0;
        HttpResponse response = null;
        for( ; attempt < MAX_ATTEMPTS; attempt++ ) {
            try {
                response = httpclient.execute( get );
                break;
            }
            catch( java.net.ConnectException e ) {
                continue; // Try again.
            }
        }

        if( response == null )
            return;

        HttpEntity entity = response.getEntity();
        if( entity == null ) 
            return;

        InputStream is = entity.getContent();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream( bos );
        try {
            IOUtil.copy( is, os );
        }
        finally {
            is.close();
            os.close();
        }

        if( "TURTLE".equals( linkType ) && url.endsWith( ".html" ) ) {
            String content = new String( bos.toByteArray(), "UTF-8" );
            handleTurtleParentFile( url, content, graph );
            return;
        }

        BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream( bos.toByteArray() ) );
        TripleStore store = Core.getInstance().getTripleStore();
        if( "TURTLE".equals( linkType ) )
            store.loadTurtle( bis, "http://invalid/", graph );
        else if( "XHTML".equals( linkType ) ) 
            store.loadRDFa( TripleStore.RdfaApi.JAVA_RDFA, TripleStore.RdfaFormat.RDFA_XHTML, bis, "http://invalid/", graph );
        else
            store.loadRDFa( TripleStore.RdfaApi.JAVA_RDFA, TripleStore.RdfaFormat.RDFA_HTML, bis, "http://invalid/", graph );

        Triple[] triplesToRemove = store.getTriplesWithSubject( "http://invalid/", graph );
        store.removeTriples( Arrays.asList( triplesToRemove ) );

        Set<String> links = extractOEAFLinks( new BufferedReader( new InputStreamReader( new ByteArrayInputStream( bos.toByteArray() ), "UTF-8" ) ) );
        for( String link : links ) {
            linkType = link.substring( 0, link.indexOf( "|" ) );
            System.out.println("link = " + link);
            link = link.substring( link.indexOf( "|" ) + 1 );
            if( !link.startsWith( "http" ) ) {
                if( link.startsWith( "/" ) )
                    link = url + link;
                else {
                    int indexOfLastSlash = url.lastIndexOf( "/" );
                    if( indexOfLastSlash == -1 )
                        link = url + "/" + link;
                    else
                        link = url.substring( 0, indexOfLastSlash ) + "/" + link;
                }

            }
            harvestFile( link, linkType, graph );
        }
    }

    private void handleTurtleParentFile( String url, String content, String... graph ) throws Exception {
        BufferedReader reader = new BufferedReader( new StringReader( content ) );
        try {
            String line;
            while( ( line = reader.readLine() ) != null ) {
                int indexOfHref = line.indexOf( "href=\"" );
                if( indexOfHref != -1 ) {
                    int indexOfStartQuote = indexOfHref + "href=\"".length();
                    int indexOfEndQuote = line.indexOf( "\"", indexOfStartQuote );
                    if( indexOfEndQuote != -1 ) {
                        String filename = line.substring( indexOfStartQuote, indexOfEndQuote );
                        String fileUrl = url.substring( 0, url.lastIndexOf( "/" ) + 1 ) + filename;
                        harvestFile( fileUrl, "TURTLE", graph ); 
                    }
                }
            }
        }
        finally {
            reader.close();
        }
    }

    public void run() {
        System.out.println( "Starting harvester: " + (new Date() ) );        

        try {
            harvestFile( url, type.toUpperCase() );

            TripleStore store = Core.getInstance().getTripleStore();
//            Triple[] triples = store.getAllTriples( "http://harvestedTriples" );
//            System.out.println( "Triples" );
//            for( int i = 0; i < triples.length; i++ )
//                System.out.println( "t["+i+"]="+triples[ i ] );
        }
        catch( Exception e ) {
            System.out.println( "PROBLEM!" );
            e.printStackTrace();
        }

        System.out.println( "Ending harvester: " + (new Date() ) );        
        
        isRunning = false;
    }

    public void start() {
        if( worker == null ) {
            worker = new Thread( Harvester.this, "PROEAF Harvester" );
            worker.start();
            isRunning = true;
        }
    }

    public void stop() {
        if( worker != null ) {
            worker.stop();
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return( isRunning );
    }

    private Thread worker;
    private boolean isRunning;

    private String name;
    private String url;
    private String type;

    private static final int MAX_ATTEMPTS = 5;

}

