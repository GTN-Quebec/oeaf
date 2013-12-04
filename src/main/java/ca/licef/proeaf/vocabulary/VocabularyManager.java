package ca.licef.proeaf.vocabulary;

import ca.licef.proeaf.core.Core;
import ca.licef.proeaf.vocabularies.COMETE;
import licef.IOUtil;
import licef.XMLUtil;
import licef.tsapi.TripleStore;
import licef.tsapi.model.Triple;
import licef.tsapi.vocabulary.RDF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-05-03
 */
public class VocabularyManager {

//    public static final String VOC_GRAPH_FULLTEXT = "voc-fullText";
//    public static final String VOC_FULLTEXT_VIEW = "voc-fullTextView";
//    public static final String VOC_GRAPH_EQUIVALENCE = "voc-equivalence";

    File vocabulariesSourceDir; //init vocabularies
    File vocabulariesDir = new File(Core.getInstance().getProeafHome(), "/conf/vocabularies");
    TripleStore tripleStore = Core.getInstance().getTripleStore();

    public void initVocabularyModule() throws Exception {
        System.out.println("Vocabulary Module initialization...");

        //init SKOS ontology
        tripleStore.loadContent(getClass().getResourceAsStream("/skos.rdf"), TripleStore.RDFXML, "skos-ontology");

        //vocs definition folder
        if (vocabulariesSourceDir == null)
            vocabulariesSourceDir = new File(getClass().getResource("/vocabularies").getFile());

        //copy initial vocabularies into PROEAF conf folder
        IOUtil.createDirectory(vocabulariesDir.getAbsolutePath());
        String[] initVocs = vocabulariesSourceDir.list();
        if (initVocs != null) {
            for (String voc : initVocs) {
                File destVoc = new File(vocabulariesDir, voc);
                if (!destVoc.exists())
                    IOUtil.copyFiles(new File(vocabulariesSourceDir, voc), destVoc);
            }
        }

        //loop on predefined vocabularies
        String[] vocs = vocabulariesDir.list();
        if (vocs != null) {
            for (String voc : vocs)
                initVocabulary(voc, false);
        }

        System.out.println("Vocabulary Module initialization done.");
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

        Triple[] res = tripleStore.getTriplesWithPredicateObject(COMETE.vocGraph, graph, true, null);
        if (res.length > 0)
            uri = res[0].getSubject();

        ArrayList<Triple> list = new ArrayList<Triple>();
        if( uri == null ) {
            //vocabulary DO creation
            newUri = Vocabulary.CoreUtil.makeURI(COMETE.VocContext.getURI());
            list.add(new Triple(newUri, RDF.type, COMETE.VocContext.getURI()));
            list.add(new Triple(newUri, COMETE.vocId, voc));
            list.add(new Triple(newUri, COMETE.vocSource, source));
            list.add(new Triple(newUri, COMETE.vocSourceLocation, location));
            list.add(new Triple(newUri, COMETE.vocGraph, graph));
            list.add(new Triple(newUri, COMETE.vocNavigable, navigableFlag + ""));

            tripleStore.insertTriples(list);
        }

        //content management
        if (newUri != null)
            uri = newUri;
        if (newUri != null || forceUpdate)
            initVocabularyContent(graph, location, uri);

        return newUri;
    }

    private void initVocabularyContent(String graph, String location, String uri) throws Exception {
        File voc = new File(vocabulariesDir, location);
        String skosContent = IOUtil.readStringFromFile(voc);
        Hashtable attributes = XMLUtil.getAttributes(skosContent, "//skos:ConceptScheme");
        String conceptSchemeVocUri = attributes.get("about").toString();
        tripleStore.insertTriple(new Triple(uri, COMETE.vocUri, conceptSchemeVocUri));
        //load content
        tripleStore.loadContent(new FileInputStream(voc), TripleStore.RDFXML, graph);

        //Generation of inferred triples
        tripleStore.doInference(graph, "skos-ontology");
    }
}
