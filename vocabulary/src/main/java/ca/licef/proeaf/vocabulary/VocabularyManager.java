package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.core.TripleStoreService;
import ca.licef.proeaf.core.util.Constants;
import ca.licef.proeaf.core.util.Triple;
import ca.licef.proeaf.vocabulary.util.Util;
import ca.licef.proeaf.vocabulary.util.XSLTUtil;
import licef.IOUtil;
import licef.StringUtil;
import licef.XMLUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-05-03
 */
public class VocabularyManager {

    public static final String VOC_GRAPH_FULLTEXT = "voc-fullText";
    public static final String VOC_FULLTEXT_VIEW = "voc-fullTextView";
    public static final String VOC_GRAPH_EQUIVALENCE = "voc-equivalence";

    private String skosRlogSrc = "/queries/skos.rlog";
    private File skosRlogFile;
    File vocabulariesSourceDir; //init vocabularies
    File vocabulariesDir = new File(Core.getInstance().getProeafHome(), "/conf/vocabularies");
    TripleStoreService tripleStore = Core.getInstance().getTripleStoreService();

    public void initVocabularies(ServletContext context) throws Exception {
        System.out.println("Vocabulary Module initialization...");

        //vocs definition folder
        if (vocabulariesSourceDir == null)
            vocabulariesSourceDir = new File(context.getRealPath("WEB-INF/classes/vocabularies"));

        //skos rules file
        skosRlogFile = new File(context.getRealPath("WEB-INF/classes/queries/skos.rlog"));

        //copy initial vocabularies into PROEAF conf folder
        if (!vocabulariesDir.exists())
            IOUtil.createDirectory(vocabulariesDir.getAbsolutePath());
        String[] initVocs = vocabulariesSourceDir.list();
        if (initVocs != null) {
            for (String voc : initVocs) {
                File destVoc = new File(vocabulariesDir, voc);
                if (!destVoc.exists())
                    IOUtil.copyFiles(new File(vocabulariesSourceDir, voc), destVoc);
            }
        }

        //graph for vocabularies equivalence relationships
        tripleStore.createGraph(VOC_GRAPH_EQUIVALENCE);

        //graphs and views for concept labels indexing
        tripleStore.createGraph(VOC_GRAPH_FULLTEXT, TripleStoreService.FULLTEXT_MODEL);
        for( int i = 0; i < TripleStoreService.INDEX_LANGUAGES.length; i++ ) {
            String lang = TripleStoreService.INDEX_LANGUAGES[ i ];
            tripleStore.createGraph(VOC_GRAPH_FULLTEXT + "_" + lang, TripleStoreService.FULLTEXT_MODEL, lang);
        }
        for( int i = 0; i < TripleStoreService.INDEX_LANGUAGES.length; i++ )
            tripleStore.createFullTextView(VOC_FULLTEXT_VIEW, VOC_GRAPH_FULLTEXT, TripleStoreService.INDEX_LANGUAGES[ i ] );

        //loop on predefined vocabularies
        ArrayList<String> newUris = new ArrayList<String>();
        String[] vocs = vocabulariesDir.list();
        if (vocs != null) {
            for (String voc : vocs) {
                String uri = initVocabulary(voc, false);
                if (uri != null)
                    newUris.add(uri);
            }
        }

        if (!newUris.isEmpty()) {
            //relationships
            for (String uri : newUris)
                initRelationships(uri);

            //global view
            initGlobalView();
        }

        System.out.println("Vocabulary Module initialization done.");
    }

    public boolean isVocabularyUsed(String uri) throws Exception {
        Hashtable<String, String>[] results =
                Core.getInstance().getTripleStoreService().getResults("getVocContextDetails.sparql", uri);
        Hashtable<String, String> vocCtxt = results[0];
        int count = tripleStore.getResultsCount( "getLOsUsingVoc.sparql", vocCtxt.get("vocUri") );
        return count > 0;
    }

    public String addNewVocabulary(String name, String source, String cat,
                                   boolean navigableFlag, String urlLocation,
                                   String fileName, InputStream uploadedInputStream) throws Exception{
        File vocDir = new File(vocabulariesDir, name);
        if (vocDir.exists())
            return "Vocabulary with name '" + name + "' already exists.";

        //create dest folder
        IOUtil.createDirectory(vocDir.getAbsolutePath());

        String location = urlLocation;
        File contentFile = null;
        if (location == null || "".equals(location)) {
            if (fileName != null && !"".equals(fileName)) {
                location = "/" + name + "/" + fileName;
                //copy content
                contentFile = new File(vocDir, fileName);
                OutputStream os = new FileOutputStream(contentFile);
                IOUtil.copy(uploadedInputStream, os);
                uploadedInputStream.close();
                os.close();
            }
        }

        //checking content
        int format = Util.getVocabularyFormatFromLocation((contentFile != null)?contentFile.getAbsolutePath():location);
        if (format == -1) {
            IOUtil.deleteDirectory(vocDir);
            return "Not a VDEX or SKOS content.";
        }

        //descriptor creation
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("vocabulary");
        doc.appendChild(root);
        Element element = doc.createElement("source");
        org.w3c.dom.Text value = doc.createTextNode(source);
        element.appendChild(value);
        root.appendChild(element);
        element = doc.createElement("category");
        value = doc.createTextNode(cat);
        element.appendChild(value);
        root.appendChild(element);
        element = doc.createElement("location");
        value = doc.createTextNode(location);
        element.appendChild(value);
        root.appendChild(element);
        if (navigableFlag) {
            element = doc.createElement("navigable");
            value = doc.createTextNode("true");
            element.appendChild(value);
            root.appendChild(element);
        }
        IOUtil.writeStringToFile(XMLUtil.getXMLString(root), new File(vocDir, "description.xml"));

        initVocabulary(name, false);
        initGlobalView();

        return null;
    }

    public String modifyVocabularyContent(String uri, InputStream uploadedInputStream) throws Exception {
        Hashtable<String, String>[] results =
                Core.getInstance().getTripleStoreService().getResults("getVocContextDetails.sparql", uri);
        String location = Vocabulary.CoreUtil.manageQuotes(results[0].get("location"));
        String vocDirName = Vocabulary.CoreUtil.manageQuotes(results[0].get("vocId"));

        File vocDir = new File(vocabulariesDir, vocDirName);

        String[] vals = StringUtil.split(location, '/');
        String contentFilename = vals[vals.length - 1];

        File tmpContentFile = new File(vocDir, contentFilename + "_tmp");
        OutputStream os = new FileOutputStream(tmpContentFile);
        IOUtil.copy(uploadedInputStream, os);
        uploadedInputStream.close();
        os.close();

        //content checking
        int format = Util.getVocabularyFormatFromLocation(tmpContentFile.getAbsolutePath());
        if (format == -1) {
            tmpContentFile.delete();
            return "Not a VDEX or SKOS content.";
        }

        FileInputStream fisTmp = new FileInputStream(tmpContentFile);
        File content = new File(vocDir, contentFilename);
        FileInputStream fis = new FileInputStream(content);
        boolean isSameContent = DigestUtils.shaHex(fisTmp).equals(DigestUtils.shaHex(fis));
        fisTmp.close();
        fis.close();
        if (isSameContent) {
            tmpContentFile.delete();
            return "Identical content.";
        }
        else {
            content.delete();
            tmpContentFile.renameTo(content);
            updateVocabulary(uri);
        }

        return null;
    }

    public void updateVocabulary(String vocContextUri) throws Exception {
        Triple[] res = tripleStore.getTriplesWithSubjectPredicate(vocContextUri, Constants.METAMODEL_VOCABULARY_ID);
        String voc = res[0].getObject();
        System.out.println("Update of vocabulary: " + voc);
        initVocabulary(voc, true);
        initRelationships(vocContextUri);
        System.out.println("Update done.");
    }

    public boolean deleteVocabulary(String uri) throws Exception {
        if (isVocabularyUsed(uri))
            return false;

        Hashtable<String, String>[] results =
                Core.getInstance().getTripleStoreService().getResults("getVocContextDetails.sparql", uri);
        Hashtable<String, String> vocCtxt = results[0];

        //triple store deletion
        String graph = Vocabulary.CoreUtil.manageQuotes(vocCtxt.get("graph"));
        Triple[] labels = tripleStore.getTriplesWithPredicate(Constants.SKOS_LABEL, graph);
        tripleStore.deleteFTTriples(labels, VOC_GRAPH_FULLTEXT); //full text triples deletion
        tripleStore.dropGraph(graph); //voc graph
        tripleStore.deleteResource(uri); //triples of voc in main graph

        //reset global view
        initGlobalView();

        //physical deletion
        String vocId = Vocabulary.CoreUtil.manageQuotes(vocCtxt.get("vocId"));
        File vocDir = new File(vocabulariesDir, vocId);
        IOUtil.deleteDirectory(vocDir);

        return true;
    }

    private String initVocabulary(String voc, boolean forceUpdate) throws Exception {
        String newUri = null;
        File vocDir = new File(vocabulariesDir, voc);
        if (!vocDir.isDirectory())
            return null;

        File descriptor = new File(vocDir, "description.xml");
        String source = null;
        String cat = null;
        String location = null;
        boolean navigableFlag = false;
        ArrayList<String> aliases = new ArrayList<String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true); //convert CDATA node to Text node
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(descriptor);
        Element top = document.getDocumentElement();
        NodeList childNodesList = top.getChildNodes();
        for (int i = 0; i < childNodesList.getLength(); i++) {
            Node node = childNodesList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                String value = e.getFirstChild().getNodeValue().trim();
                if ("source".equals(e.getTagName()))
                    source = value;
                if ("category".equals(e.getTagName()))
                    cat = value;
                if ("location".equals(e.getTagName()))
                    location = value;
                if ("navigable".equals(e.getTagName()))
                    navigableFlag = "true".equals(value);
                if ("alias".equals(e.getTagName()))
                    aliases.add(value);
            }
        }

        String uri = null;
        String graph = "voc_" + (source + "_" + cat).toLowerCase();
        String vocUri = Core.getInstance().getUriPrefix() + "/voc/" + source.toLowerCase() + "/" + cat;
        Hashtable<String, String>[] res = tripleStore.getResults("getVocContext.sparql", graph);

        if (res.length > 0) {
            uri = res[0].get("s");
        }

        ArrayList<Triple> list = new ArrayList<Triple>();
        if( uri == null ) {
            //vocabulary DO creation
//            newUri = Vocabulary.CoreUtil.makeURI(fedoraId, Constants.TYPE_VOCABULARY_CONTEXT);
            newUri = Vocabulary.CoreUtil.makeURI(null, Constants.TYPE_VOCABULARY_CONTEXT);
            list.add(new Triple(newUri, Constants.TYPE, Constants.TYPE_VOCABULARY_CONTEXT));
            list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_ID, voc));
            list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_SOURCE, source));
            list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_SOURCE_LOCATION, location));
            list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_GRAPH, graph));
            list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_NAVIGABLE, navigableFlag + ""));

            for (String alias : aliases)
                list.add(new Triple(newUri, Constants.METAMODEL_VOCABULARY_ALIAS, alias));
            tripleStore.addTriples(list);
        }

        //content management
        if (newUri != null)
            uri = newUri;
        if (newUri != null || forceUpdate)
            initVocabularyContent(graph, location, vocUri, navigableFlag, uri, newUri == null, "Vocabulary:init");

        return newUri;
    }

    private void initVocabularyContent(String graph, String location, String vocUri,
                                       boolean indexLabels, String uri, boolean isUpdate,
                                       String logMessage) throws Exception {

        //update means restore state like before 1st init
        if (isUpdate) {
            //clean voc uri
            Triple[] triples = tripleStore.getTriplesWithSubjectPredicate(uri, Constants.METAMODEL_VOCABULARY_URI);
            tripleStore.deleteTriples(triples);

            //remove indexed labels
            Triple[] labels = tripleStore.getTriplesWithPredicate(Constants.SKOS_LABEL, graph);
            tripleStore.deleteFTTriples(labels, VOC_GRAPH_FULLTEXT);

            //drop the graph
            tripleStore.dropGraph(graph);
        }

        //format resources manipulation
//        String conceptSchemeVocUri = setVocDatastream(fedoraId, location, vocUri, isUpdate, logMessage);
        String conceptSchemeVocUri = setVocDatastream(null, location, vocUri, isUpdate, logMessage);
        if (conceptSchemeVocUri != null) //done here to catch external vocUris inside vocab
            tripleStore.addTriple(uri, Constants.METAMODEL_VOCABULARY_URI, conceptSchemeVocUri);

        //graph creation and population
        tripleStore.createGraph(graph);
/*
        String fedoraPort = Core.getInstance().getFedoraPort();
        String fedoraDOUrl = "http://localhost" +
                ("80".equals(fedoraPort)?"":":" + fedoraPort) + "/fedora/get/" + fedoraId;
        tripleStore.loadRDFContent(fedoraDOUrl + "/" + Constants.DATASTREAM_SKOS, graph);
*/

        //Generation of inferred triples
        tripleStore.applyRules(skosRlogFile.toURI().toString(), graph);

        //indexing of concept labels for navigable vocs
        if (indexLabels) {
            Triple[] labels = tripleStore.getTriplesWithPredicate(Constants.SKOS_LABEL, graph);
            tripleStore.addFTTriples(labels, VOC_GRAPH_FULLTEXT);
        }
    }

    private String setVocDatastream(String id, String location, String vocUri, boolean update, String logMessage) throws Exception {
        if (update && !hasVocabularyChanged(id, location)) //if same voc version, quit
            return null;

        String vocContent;
        String skosContent;
        URL sourceUrl;

        if (IOUtil.isURL(location)) {
            sourceUrl = new URL( location );
            vocContent = IOUtil.readStringFromURL(sourceUrl);
        }
        else
            vocContent = IOUtil.readStringFromFile(new File( vocabulariesDir, location));

        int format = Util.getVocabularyFormat(vocContent);
        switch (format) {
            case Util.VDEX_FORMAT : //keep vdex version for history
/*
                if (update)
                    fedora.modifyDatastream(id, Constants.DATASTREAM_VDEX, vocContent, "Vocabulary:reset");
                else
                    fedora.addDatastream(id, Constants.DATASTREAM_VDEX, Constants.DATASTREAM_VDEX_LABEL, true,
                            vocContent, "text/xml", null, "M", logMessage);
*/
                skosContent = convertVdexToSkos(vocContent, vocUri);
                break;
            default:
                skosContent = vocContent;
        }
        //save skos datastream
/*
        if (update)
            fedora.modifyDatastream(id, Constants.DATASTREAM_SKOS, skosContent, logMessage);
        else
            fedora.addDatastream(id, Constants.DATASTREAM_SKOS, Constants.DATASTREAM_SKOS_LABEL, true,
                    skosContent, "text/xml", null, "M", logMessage);
*/

        //return vocURI with xpath
        Hashtable attributes = XMLUtil.getAttributes(skosContent, "//skos:ConceptScheme");
        return attributes.get("about").toString();
    }

    //Relationships management
    private void initRelationships(String uri) throws Exception {
        String location = tripleStore.getTriplesWithSubjectPredicate(uri,
                Constants.METAMODEL_VOCABULARY_SOURCE_LOCATION)[0].getObject();
        XSLTUtil.initVocUris();
        File rels = getRelationships(location);
        if (rels != null) {
            tripleStore.loadRDFContent(rels.toURI().toString(), VOC_GRAPH_EQUIVALENCE);
            tripleStore.applyRules(skosRlogFile.toURI().toString(), VOC_GRAPH_EQUIVALENCE);
        }
    }

    private File getRelationships(String location) throws Exception {
        String voc = location.split("/")[1];
        File relDescr = new File(vocabulariesDir, voc + "/relationships.rdf");

        if (!relDescr.exists()) {
            String vocContent = "";
            URL sourceUrl;
            if (IOUtil.isURL(location)) {
                sourceUrl = new URL( location );
                vocContent = IOUtil.readStringFromURL(sourceUrl);
            }
            else
                vocContent = IOUtil.readStringFromFile(new File(vocabulariesDir, location));

            relDescr = null;
            String skosContent = "";
            int format = Util.getVocabularyFormat(vocContent);
            switch (format) {
                case Util.VDEX_FORMAT : //keep vdex version for history
                    skosContent = convertVdexRelationshipsToSkos(vocContent);
                    break;
            }
            if (skosContent.contains("Match")) {
                relDescr = new File(System.getProperty("java.io.tmpdir"), "relationships.rdf");
                IOUtil.writeStringToFile(skosContent, relDescr);
            }
        }

        return relDescr;
    }

    //global view management
    //view creation/reset of all vocabularies
    public void initGlobalView() throws Exception {
        Hashtable<String, String>[] results = tripleStore.getResults("getVocContexts.sparql");
        String[] graphs = new String[results.length + 1];
        int i = 0;
        for (; i < results.length; i++)
            graphs[i] = Vocabulary.CoreUtil.manageQuotes(results[i].get("graph"));
        graphs[i] = VOC_GRAPH_EQUIVALENCE;

        tripleStore.dropGraph(TripleStoreService.VOC_GLOBAL_VIEW);
        tripleStore.createView(TripleStoreService.VOC_GLOBAL_VIEW, graphs);
    }

    public String convertVdexToSkos( String vdexContent, String vocUri ) throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put( "vocabularyUri", vocUri );
        StreamSource source = new StreamSource( new ByteArrayInputStream( vdexContent.getBytes() ) );
        return( Vocabulary.CoreUtil.applyXslToDocument( "convertVDEXToSKOS", source, params ) );
    }

    public String convertVdexRelationshipsToSkos( String vdexContent ) throws Exception {
        StreamSource source = new StreamSource( new ByteArrayInputStream( vdexContent.getBytes() ) );
        return( Vocabulary.CoreUtil.applyXslToDocument( "convertVDEXRelationshipsToSKOS", source ) );
    }

    boolean hasVocabularyChanged(String id, String location) throws Exception {
        String vocContent;
        URL sourceUrl;

        if (IOUtil.isURL(location)) {
            sourceUrl = new URL( location );
            vocContent = IOUtil.readStringFromURL(sourceUrl);
        }
        else
            vocContent = IOUtil.readStringFromFile(new File( vocabulariesDir, location));

        int format = Util.getVocabularyFormat(vocContent);
//        String locationFormat = Constants.DATASTREAM_SKOS;
//        if (format == Util.VDEX_FORMAT)
//            locationFormat = Constants.DATASTREAM_VDEX;

        //current voc datastream
//        String previousVocContent = fedora.getDatastream(id, locationFormat);
        //  todo read from file
        String previousVocContent = "";

        return !DigestUtils.shaHex(previousVocContent).equals(DigestUtils.shaHex(vocContent));
    }

    public ArrayList<String> getVocsWithAvailableUpdate() throws Exception {
        ArrayList<String> vocsWithUpdate = new ArrayList<String>();

        Hashtable<String, String>[] results = tripleStore.getResults("getVocContextsDetails.sparql");

        for (Hashtable<String, String> result : results) {
            if ( hasVocabularyChanged(result.get("doId"), Vocabulary.CoreUtil.manageQuotes(result.get("location"))) )
                vocsWithUpdate.add(result.get("s"));
        }
        return vocsWithUpdate;
    }
}
