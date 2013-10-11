package ca.licef.proeaf.core.util;

import ca.licef.proeaf.core.Core;
import licef.CommonNamespaceContext;
import licef.IOUtil;
import licef.StringUtil;
import licef.XMLUtil;
import licef.jrdf.JRDFFactory;
import licef.jrdf.SortedMemoryJRDFFactory;
import licef.jrdf.collection.MemMapFactory;
import licef.jrdf.graph.Graph;
import licef.jrdf.graph.GraphException;
import licef.jrdf.graph.TripleFactory;
import licef.jrdf.writer.*;
import licef.jrdf.writer.rdfxml.RdfXmlWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 8-Sep-2011
 */
public class Util {

    public static String readString(InputStream is) throws IOException {
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
        }
        reader.close();
        return contents.toString();
    }

    public static void writeString(OutputStream os, String text) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(text);
        writer.flush();
        writer.close();
    }

    /* ID conversion and management*/

    public static String getNewId() {
        return UUID.randomUUID().toString();
    }

    public static String getIdNumberValue(String id) {
        String[] vals = StringUtil.split(id, '/');
        String[] val = StringUtil.split(vals[vals.length - 1], ':');
        return val[val.length - 1];
    }

    public static String makeURI(String type) {
        return makeURI(getNewId(), type);
    }

    public static String makeURI(String id, String type) {
        if (id.startsWith("http://"))
            return id;
        String typeVal = getTypeLabel(type);
        return Core.getInstance().getUriPrefix() + "/" + typeVal + "/" + getIdNumberValue(id);
    }

    /**
     * Returns a condensed form of an uri
     * @param uri
     * example : "http://server/ld/resource/person/3" returns "person:3"
     */
    public static String makeCondensedFormat(String uri) {
        if (!uri.startsWith("http"))
            return uri;
        String type = getURIType(uri);
        String val = getIdNumberValue(uri);
        return getTypeLabel(type) + ":" +val;
    }

    public static String makeUriFromCondensedFormat(String id) {
        if (id.startsWith("http"))
            return id;
        String[] vals = StringUtil.split(id, ':');
        return makeURI(vals[1], getTypeFromLabel(vals[0]));
    }

    public static String getTypeLabel(String type) {
        String typeVal = null;
/*
        if (Constants.TYPE_METADATA_RECORD.equals(type))
            typeVal = "metadatarecord";
        else if (Constants.TYPE_LEARNING_OBJECT.equals(type))
            typeVal = "learningobject";
        else if (Constants.TYPE_PERSON.equals(type))
            typeVal = "person";
        else if (Constants.TYPE_ORGANIZATION.equals(type))
            typeVal = "organization";
        else if (Constants.TYPE_REPOSITORY.equals(type))
            typeVal = "repository";
        else if (Constants.TYPE_VOCABULARY_CONTEXT.equals(type))
            typeVal = "voccontext";
*/
        return typeVal;
    }

    public static String getRestUrl(String type) {
        Core core = Core.getInstance();
        String url = null;
/*
        if (Constants.TYPE_METADATA_RECORD.equals(type))
            url = core.getMetadataUrl() + "/rest/metadataRecords";
        else if (Constants.TYPE_LEARNING_OBJECT.equals(type))
            url = core.getMetadataUrl() + "/rest/learningObjects";
        else if (Constants.TYPE_IDENTITY.equals(type))
            url = core.getIdentityUrl() + "/rest/identities";
        else if (Constants.TYPE_PERSON.equals(type))
            url = core.getIdentityUrl() + "/rest/persons";
        else if (Constants.TYPE_ORGANIZATION.equals(type))
            url = core.getIdentityUrl() + "/rest/organizations";
        else if (Constants.TYPE_REPOSITORY.equals(type))
            url = core.getMetadataUrl() + "/rest/repositories";
        else if (Constants.TYPE_VOCABULARY_CONTEXT.equals(type))
            url = core.getVocabularyUrl() + "/rest/vocContexts";
        else if (Constants.TYPE_VOCABULARY.equals(type))
            url = core.getVocabularyUrl() + "/rest/voc";
*/
        return url;
    }

    public static String getURIType(String uri) {
        String prefix = Core.getInstance().getUriPrefix();
        int indexOfPrefix = uri.indexOf( prefix );
        if( indexOfPrefix == -1 )
            return( null );

        String uriWithoutPrefix = uri.substring( indexOfPrefix + prefix.length() );
        String[] vals = uriWithoutPrefix.split( "/" );
        if( vals.length < 2 )
            return( null );
        String label = vals[1];
/*
        if( "voc".equals( label ) ) {
            if( vals.length == 4 )
                return( Constants.TYPE_VOCABULARY );
            else if( vals.length == 5 )
                return( Constants.TYPE_VOCABULARY_CONCEPT );
            else
                return( null );
        }
*/
        return getTypeFromLabel(label);
    }

    public static String getTypeFromLabel(String label) {
        String type = null;
/*
        if ("metadatarecord".equals(label))
            type = Constants.TYPE_METADATA_RECORD;
        else if ("learningobject".equals(label))
            type = Constants.TYPE_LEARNING_OBJECT;
        else if ("person".equals(label))
            type = Constants.TYPE_PERSON;
        else if ("organization".equals(label))
            type = Constants.TYPE_ORGANIZATION;
        else if ("voccontext".equals(label))
            type = Constants.TYPE_VOCABULARY_CONTEXT;
*/
        return type;
    }


    public static String buildFilterConstraints(String[] values, String varName, boolean resourceValues, String test, String delimiter) {
        return buildFilterConstraints(Arrays.asList(values), varName, resourceValues, test, delimiter);
    }

    public static String buildFilterConstraints(List<String> values, String varName, boolean resourceValues, String test, String delimiter) {
        String _delimiter = "";
        StringBuilder constraints = new StringBuilder();
        for (Iterator it = values.iterator(); it.hasNext();) {
            String val = it.next().toString();
            constraints.append( _delimiter );
            constraints.append( "?" ).append( varName ).append( " " ).append( test );
            if (resourceValues)
                constraints.append( " <" ).append( val ).append( ">" );
            else
                constraints.append( " \"" ).append( val ).append( "\"" );
            _delimiter = " " + delimiter + " ";
        }
        return( constraints.toString() );
    }

    public static List<String> buildList( Hashtable<String, String>[] results, String varName ) throws Exception{
        ArrayList<String> list = new ArrayList<String>();
        for (Hashtable<String, String> lo : results) {
            list.add(lo.get(varName));
        }
        return list;
    }

    /* OLD Triples manipulation*/

    public static Triple[] getTriplesPO( String subject, String json) throws Exception {
        return getTriples(subject, null, null, json);
    }

    public static Triple[] getTriplesSP(String object, String json) throws Exception{
        return getTriples(null, null, object, json);
    }

    public static Triple[] getTriplesS(String predicate, String object, String json) throws Exception    {
        return getTriples(null, predicate, object, json);
    }

    public static Triple[] getTriplesO(String subject, String predicate, String json) throws Exception{
        return getTriples(subject, predicate, null, json);
    }

    public static Triple[] getTriples(String subject, String predicate, String object, String json) throws Exception{
        ArrayList<Triple> triples = new ArrayList<Triple>();
        JSONObject jsonObj = new JSONObject(json);
        JSONArray jsonArray = jsonObj.getJSONArray("results");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = (JSONObject)jsonArray.get(i);
            Triple triple = new Triple( subject == null?obj.getString("s"):subject,
                                        predicate == null?obj.getString("p"):predicate,
                                        object == null?obj.getString("o"):object);
            triples.add( triple );
        }
        return( triples.toArray(new Triple[triples.size()]) );
    }

    /* Results manipulation */

    public static Hashtable<String, String>[] getResults(String json) throws Exception{
        ArrayList<Hashtable<String, String>> list = new ArrayList<Hashtable<String, String>>();
        JSONObject jsonObj = new JSONObject(json);
        JSONArray jsonArray = jsonObj.getJSONArray("results");
        for (int i = 0; i < jsonArray.length(); i++) {
            Hashtable<String, String> t = new Hashtable<String, String>();
            JSONObject obj = (JSONObject)jsonArray.get(i);
            for (Iterator it = obj.keys(); it.hasNext();) {
                String key = (String)it.next();
                String val = (String)obj.get(key);
                if (val.startsWith("info:fedora/"))
                    val = val.substring("info:fedora/".length());
                t.put(key, val);
            }
            list.add(t);
        }
        Hashtable<String, String>[] results = new Hashtable[list.size()];
        for (int i = 0; i < list.size(); i++)
            results[i] = list.get(i);
        
        return results;
    }

    /*
     * @param queryId Name of the query that corresponds to a file name in the query resource directory.
     * @param params Parameters which their value will be substituted in the query.  Beware, numeric values should be 
     * converted to string before the call to prevent locale-dependant formatting.
     */
    public static String getQuery( String queryId, Object... params ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream( baos );
        InputStream is = Core.getInstance().getClass().getResourceAsStream(Constants.QUERY_BASE_LOCATION + "/" + queryId );

        BufferedInputStream bis = new BufferedInputStream( is );
        try {
            IOUtil.copy( bis, bos );
        }
        finally {
            bis.close();
            bos.close();
        }
        String rawQuery = baos.toString( "UTF-8" );
        if( params == null || params.length == 0 )
            return( rawQuery );

        String query = MessageFormat.format( rawQuery, params );
        return( query );
    }

    /*
     * Metadatas
     */
    public static String generateMetaMetadataIdentifier( String recordId, String metadataFormat ) {
        StringBuilder str = new StringBuilder();
        str.append( "urn:" );
        str.append( Core.getInstance().getRepositoryNamespace() ).append( ":" );
        str.append( recordId ).append( ":" );
        str.append( metadataFormat );
        return( str.toString() );
    }

    public static String generateGeneralIdentifier( String recordId ) {
        StringBuilder str = new StringBuilder();
        str.append( "urn:" );
        str.append( Core.getInstance().getRepositoryNamespace() ).append( ":" );
        str.append( recordId );
        return( str.toString() );
    }

    public static String getNewLomXml( String recordId ) {
        String metaMetadataIdentifierCatalog = "URI";
        String metaMetadataIdentifierEntry = generateMetaMetadataIdentifier( recordId, "lom" );
        String generalIdentifierCatalog = Core.getInstance().getRepositoryName();
        String generalIdentifierEntry = generateGeneralIdentifier( recordId );

        StringBuilder xml = new StringBuilder();
        xml.append( "<lom:lom xmlns:lom=\"" + licef.CommonNamespaceContext.lomNSURI + "\">" );
        xml.append( "<lom:general><lom:identifier>" );
        xml.append( "<lom:catalog>" ).append( generalIdentifierCatalog ).append( "</lom:catalog>" );
        xml.append( "<lom:entry>" ).append( generalIdentifierEntry ).append( "</lom:entry>" );
        xml.append( "</lom:identifier></lom:general>" );
        xml.append( "<lom:metaMetadata><lom:identifier>" );
        xml.append( "<lom:catalog>" ).append( metaMetadataIdentifierCatalog ).append( "</lom:catalog>" );
        xml.append( "<lom:entry>" ).append( metaMetadataIdentifierEntry ).append( "</lom:entry>" );
        xml.append( "</lom:identifier></lom:metaMetadata>" );
        xml.append( "</lom:lom>" );
        return( xml.toString() );
    }

    public static String getNewDcXml( String recordId ) {
        String identifier = generateGeneralIdentifier( recordId );
        StringBuilder xml = new StringBuilder();
        xml.append( "<oaidc:dc xmlns:oaidc=\"" + licef.CommonNamespaceContext.oaidcNSURI + "\" xmlns:dc=\"" +
            licef.CommonNamespaceContext.dcNSURI + "\">" );
        xml.append( "<dc:identifier>" ).append( identifier ).append( "</dc:identifier>" );
        xml.append( "</oaidc:dc>" );
        return( xml.toString() );
    }

    public static String getNewRelsExtXml( String recordId ) {
        if (!recordId.startsWith("info:fedora/"))
            recordId = "info:fedora/" + recordId;
        StringBuilder xml = new StringBuilder();
        xml.append( "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" );
        xml.append( "<rdf:Description rdf:about=\"" + recordId + "\" />" );
        xml.append( "</rdf:RDF>" );
        return( xml.toString() );
    }

    public static String applyXslToDocument( String xsltBaseFilename, StreamSource doc, Properties transformerProps, HashMap<String,String> params, Locale locale ) throws IOException, TransformerConfigurationException, TransformerException {
        if( locale != null ) {
            if( "fr".equals( locale.getLanguage() ) )
                xsltBaseFilename = xsltBaseFilename + "_" + locale.getLanguage();
        }
        String xsltFile = "/xslt/" + xsltBaseFilename + ".xsl";
        StreamSource xslt = new StreamSource( Util.class.getResourceAsStream( xsltFile ) );
        return( XMLUtil.applyXslToDocument( xslt, doc, resolver, transformerProps, params ) ); 
    }

    public static String applyXslToDocument( String xsltBaseFilename, StreamSource doc, HashMap<String,String> params, Locale locale ) throws IOException, TransformerConfigurationException, TransformerException {
        return( applyXslToDocument( xsltBaseFilename, doc, null, params, locale ) ); 
    }

    public static String applyXslToDocument( String xsltBaseFilename, StreamSource doc, HashMap<String,String> params ) throws IOException, TransformerConfigurationException, TransformerException {
        return( applyXslToDocument( xsltBaseFilename, doc, null, params, null ) );
    }

    public static String applyXslToDocument( String xsltBaseFilename, StreamSource doc ) throws IOException, TransformerConfigurationException, TransformerException {
        return( applyXslToDocument( xsltBaseFilename, doc, null, null, null ) );
    }

    public static String getTriplesAsRdf( Collection<Triple> triples ) throws IOException, GraphException, TransformerConfigurationException, TransformerException {
        Triple[] tripleArray = triples.toArray( new Triple[ triples.size() ] );
        return( getTriplesAsRdf( tripleArray ) );
    }

    public static String getTriplesAsRdf( Triple[] tripleArray ) throws IOException, GraphException, TransformerConfigurationException, TransformerException {
        Graph graph = makeJrdfGraph( tripleArray );

        BlankNodeRegistry nodeRegistry = new MappedBlankNodeRegistry( new MemMapFactory() );
        RdfWriter writer = new RdfXmlWriter( nodeRegistry, getRdfNamespaceMap() );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream( os );
        try {
            try {
                writer.write( graph, bos );
            } finally {
                writer.close();
            }
        } finally {
            bos.close();
        }
        String rdf = os.toString( "UTF-8" );
        StreamSource source = new StreamSource( new BufferedReader( new StringReader( rdf ) ) );
        String prettyRdf = applyXslToDocument( "removeUnusedNamespaces", source ); 

        return( prettyRdf );
    }

    private static RdfNamespaceMap rdfNamespaceMap;
    
    private static RdfNamespaceMap getRdfNamespaceMap() {
        if( rdfNamespaceMap == null ) {
            MemMapFactory mapFactory = new MemMapFactory();
            rdfNamespaceMap = new RdfNamespaceMapImpl( mapFactory );
            System.setProperty( RdfXmlWriter.WRITE_LOCAL_NAMESPACE, "true" );
            for( Iterator it = CommonNamespaceContext.getInstance().getAllPrefixes(); it.hasNext(); ) {
                String prefix = (String)it.next();
                String namespace = CommonNamespaceContext.getInstance().getNamespaceURI( prefix );
                try {
                    rdfNamespaceMap.addNamespace( prefix, namespace );
                }
                catch( NamespaceException ignore ) {
                    // Ignore namespace that we cannot add.
                }
            }
        }
        return( rdfNamespaceMap );
    }

    private static Graph makeJrdfGraph( Triple[] tripleArray ) {
        JRDFFactory jrdfFactory = SortedMemoryJRDFFactory.getFactory();
        Graph graph = jrdfFactory.getGraph();
        TripleFactory tripleFactory = graph.getTripleFactory();

        for( Triple triple : tripleArray ) {
            try {
                URI subject = new URI( triple.getSubject() );
                URI predicate = new URI( triple.getPredicate() );
                if( triple.isLiteral() ) {
                    String literal = triple.getObject();
                    if( triple.getLanguage() == null )
                        tripleFactory.addTriple( subject, predicate, literal );
                    else
                        tripleFactory.addTriple( subject, predicate, literal, triple.getLanguage() );
                }
                else {
                    URI object = new URI( triple.getObject() );
                    tripleFactory.addTriple( subject, predicate, object );
                }
            }
            catch( URISyntaxException ignore ) {
                // The invalid triple is not added to the graph.
            }
        }

        return( graph );
    }

    private static URIResolver resolver = new URIResolver() {
        public Source resolve( String href, String base ) {
            InputStream is = getClass().getResourceAsStream( href );
            if( is == null )
                is = getClass().getResourceAsStream( "/" + href );
            if( is == null )
                is = getClass().getResourceAsStream( "/xslt/" + href );

            return( new StreamSource( is ) );
        }
    };

    public static String encodeSingleQuote(String str) {
        str = str.replaceAll("\\\\'", "'"); //first
        str = str.replaceAll("\\\\'", " \'"); //2nd step
        return str.replaceAll("'", "\\\\'");
    }

    public static boolean isDate(String str) {
        return (str.endsWith("^^" + Constants.XSD_DATE_TYPE));
    }

    public static String manageDateString(String str) {
        if (isDate(str))
            str = manageQuotes( str.substring(0, str.indexOf("^^" + Constants.XSD_DATE_TYPE)) );
        return str;
    }

    public static String[] manageLocalizedString(String str) {
        String content = str;
        String lang = null;
        Matcher m = localizedStringPattern.matcher( str );
        if (m.find()) {
            int i = str.lastIndexOf('@');
            content = str.substring(0, i);
            lang = str.substring(i + 1);
        } 
        content = manageQuotes(content);
        return new String[]{content, lang};
    }

    public static String manageQuotes(String str) {
        str = StringUtil.unquote(str);
        return str.replace("\\\"", "\"");
    }

    public static String formatLanguage(String lang) {
        String[] vals  = StringUtil.split(lang, '-');
        String res = vals[0].substring(0, 2);
        if (vals.length == 2)
            res += "-" + vals[1].substring(0, 2);
        return res;
    }

    public static Dimension getPhotoDimension( String location ) {
        ImageInputStream in = null;
        try {
            in = ImageIO.createImageInputStream( new URL( location ).openStream() );
            final Iterator readers = ImageIO.getImageReaders( in );
            if( readers.hasNext() ) {
                ImageReader reader = (ImageReader)readers.next();
                try {
                    reader.setInput( in );
                    return( new Dimension( reader.getWidth( 0 ), reader.getHeight( 0 ) ) );
                }
                finally {
                    reader.dispose();
                }
            }
        }
        catch( IOException e ) {
            return( null );
        }
        finally {
            if( in != null ) {
                try {
                    in.close();
                }
                catch( IOException e ) {
                    e.printStackTrace();
                }
            }
        }
        return( null );
    }

    public static String formatKeywords(String keywords) {
        String quoteEsc = "SINGLEQUOTEREGEXESC";
        String dblQuoteEsc = "DBLQUOTEREGEXESC";
        keywords = keywords.replace("\'", quoteEsc);
        keywords = keywords.replace("\"", dblQuoteEsc);

        int index = keywords.indexOf(dblQuoteEsc);
        int cp = 0;
        while (index != -1) {
            cp++;
            index = keywords.indexOf(dblQuoteEsc, index + 1);
        }
        if ((cp % 2) != 0)
            keywords += dblQuoteEsc;

        String keywordsFormattedForRegex = keywords.replaceAll( "[^\\p{L}\\p{N}]", " " );

        //I think it can be removed... -AM
        keywordsFormattedForRegex = keywordsFormattedForRegex.replace( dblQuoteEsc, "\\\"" );
        keywordsFormattedForRegex = keywordsFormattedForRegex.replace( quoteEsc, "\\\'" );

        return keywordsFormattedForRegex;
    }

    public static String getSyntaxHighlightedCode( String language, String code ) throws IOException, TransformerConfigurationException, ParserConfigurationException, TransformerException {
        File tempSyntaxHighlighterFile = createSyntaxHighlighterFile( language, code );

        HashMap<String,String> params = new HashMap<String,String>();
        String configFileLocation = getSyntaxHighlighterConfigFileLocation();
        if( configFileLocation != null )
            params.put( "xslthl.config", configFileLocation );

        StreamSource source = new StreamSource( new BufferedInputStream( new FileInputStream( tempSyntaxHighlighterFile ) ) );
        String result = Util.applyXslToDocument( "highlightSyntax", source, params ); 

        if( !tempSyntaxHighlighterFile.delete() )
            System.err.println( "Cannot delete temp file: " + tempSyntaxHighlighterFile );

        return( result );
    }

    private static String getSyntaxHighlighterConfigFileLocation() {
        if( syntaxHighlighterConfigFileLocation == null ) {
            try {
                File configFile = new File( System.getProperty( "java.io.tmpdir" ), "xslthl-config.xml" );

                if( configFile.exists() ) {
                    if( !configFile.delete() )
                        System.err.println( "Cannot delete temp file: " + configFile );
                }

                InputStream is = Util.class.getResourceAsStream( "/xslthl-config.xml" );
                OutputStream os = new FileOutputStream( configFile );
                try {
                    IOUtil.copy( is, os );
                }
                finally {
                    if( os != null )
                        os.close();
                    if( is != null )
                        is.close();
                }

                syntaxHighlighterConfigFileLocation = ( new URL( "file:///" + configFile ) ).toString();
            }
            catch( IOException e ) {
                // Syntax highlighting will not work.
                return( null );
            }
        }
        return( syntaxHighlighterConfigFileLocation );
    }

    private static File createSyntaxHighlighterFile( String language, String code ) throws IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();

        Element rootElement = doc.createElement( "document" );
        doc.appendChild( rootElement );

        Element codeElement = doc.createElement( "code" );
        codeElement.setAttribute( "language", language );
        CDATASection xmlAsCData = doc.createCDATASection( code );
        codeElement.appendChild( xmlAsCData );

        rootElement.appendChild( codeElement );

        String inputData = XMLUtil.getXMLString( doc );

        File outputFile = File.createTempFile( "syntaxHighlighting", ".xml" );
        IOUtil.writeStringToFile( inputData, outputFile );
        return( outputFile );
    }

    static private Pattern localizedStringPattern = Pattern.compile( "\".+\"@.+", Pattern.DOTALL );
    static private String syntaxHighlighterConfigFileLocation = null;

}
