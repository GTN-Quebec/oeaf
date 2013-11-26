package ca.licef.proeaf.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 8-Sep-2011
 */
public class Constants {

    public static final String[] INDEX_LANGUAGES = { "en", "fr", "es" };

    public static final String[] UI_LANGUAGES = { "en", "fr" };

    //Path
    public static final String QUERY_BASE_LOCATION = "/queries";

    //General
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String XSD_DATE_TYPE = "<http://www.w3.org/2001/XMLSchema#date>";
    
    //Standards
    public static final String OAI_DC_NAMESPACE = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    public static final String IEEE_LOM_NAMESPACE = "http://ltsc.ieee.org/xsd/LOM";

    //Mimetypes
    public static final String VCARD_MIMETYPE = "text/x-vcard";
    public static final String TEXT_MIMETYPE = "text/plain";
    static final String[] _MIMETYPE_CATEGORIES = new String[] {
            "application", "audio", "image", "text", "video"
    };
    public static final List MIMETYPE_CATEGORIES = Arrays.asList(_MIMETYPE_CATEGORIES);

    //**********
    // Classes
    //**********
    public static final String OEAF_LEARNING_OPPORTUNITY = "http://normetic.org/uri/profil_oeaf/v1.0/ns#rc0004";
    public static final String OEAF_CONCRETE_LEARNING_OPPORTUNITY = "http://normetic.org/uri/profil_oeaf/v1.0/ns#rc0003";
    public static final String OEAF_GENERIC_LEARNING_OPPORTUNITY = "http://normetic.org/uri/profil_oeaf/v1.0/ns#rc0002";
    public static final String TYPE_VOCABULARY_CONTEXT = "http://comete.licef.ca/reference#VocContext";
    public static final String TYPE_VOCABULARY = "http://www.w3.org/2004/02/skos/core#ConceptScheme";
    public static final String TYPE_VOCABULARY_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";
    public static final String FOAF_DOCUMENT = "http://xmlns.com/foaf/0.1/Document";

    //************
    // Predicates
    //************
    public static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    public static final String FOAF_PRIMARY_TOPIC = "http://xmlns.com/foaf/0.1/primaryTopic";
    public static final String FOAF_IS_PRIMARY_TOPIC_OF = "http://xmlns.com/foaf/0.1/isPrimaryTopicOf";

    public static final String OEAF_IDENTIFIER = "http://normetic.org/uri/profil_oeaf/v1.0/ns#sed0900";
    public static final String MLR_TITLE = "http://normetic.org/iso-iec/19788-2/2012/DES0100";
    public static final String OEAF_DESCRIPTION = "http://normetic.org/uri/profil_oeaf/v1.0/ns#sed1100";

    public static final String SKOS_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    public static final String SKOS_TOP_CONCEPT_OF = "http://www.w3.org/2004/02/skos/core#topConceptOf";
    public static final String SKOS_NARROWER = "http://www.w3.org/2004/02/skos/core#narrower";
    public static final String SKOS_CLOSE_MATCH = "http://www.w3.org/2004/02/skos/core#closeMatch";
    public static final String SKOS_RELATED_MATCH = "http://www.w3.org/2004/02/skos/core#relatedMatch ";

    //Metamodel Vocabulary
    public static final String METAMODEL_VOCABULARY_ID = "http://comete.licef.ca/reference#vocId";
    public static final String METAMODEL_VOCABULARY_SOURCE = "http://comete.licef.ca/reference#vocSource";
    public static final String METAMODEL_VOCABULARY_SOURCE_LOCATION = "http://comete.licef.ca/reference#vocSourceLocation";
    public static final String METAMODEL_VOCABULARY_URI = "http://comete.licef.ca/reference#vocUri";
    public static final String METAMODEL_VOCABULARY_GRAPH = "http://comete.licef.ca/reference#vocGraph";
    public static final String METAMODEL_VOCABULARY_NAVIGABLE = "http://comete.licef.ca/reference#vocNavigable";
    public static final String METAMODEL_VOCABULARY_ALIAS = "http://comete.licef.ca/reference#vocAlias";

    //Namespace Sets
    static final String[] _LITERAL_PREDICATES = new String[] {
//        LABEL, OEAF_IDENTIFIER, MLR_TITLE, OEAF_DESCRIPTION,
//        METAMODEL_VOCABULARY_ID, METAMODEL_VOCABULARY_SOURCE, METAMODEL_VOCABULARY_SOURCE_LOCATION, METAMODEL_VOCABULARY_GRAPH,
//        METAMODEL_VOCABULARY_NAVIGABLE, METAMODEL_VOCABULARY_ALIAS, SKOS_LABEL
    };

    static final String[] _FULL_TEXT_PREDICATES = new String[] {
//        LABEL, MLR_TITLE, OEAF_DESCRIPTION
    };

    static final String[] _DATE_PREDICATES = new String[] {
//        METAMODEL_ADDED, METAMODEL_UPDATED, METAMODEL_CREATED
    };

    public static final List LITERAL_PREDICATES = Arrays.asList(_LITERAL_PREDICATES);
    public static final List FULL_TEXT_PREDICATES = Arrays.asList(_FULL_TEXT_PREDICATES);
    public static final List DATE_PREDICATES = Arrays.asList(_DATE_PREDICATES);

}
