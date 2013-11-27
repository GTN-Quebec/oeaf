package ca.licef.proeaf.vocabulary.util;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 19-Mar-2012
 */

public class Util {

    //Standards
    public static final String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
    public static final String VDEX_NAMESPACE = "http://www.imsglobal.org/xsd/imsvdex_v1p0";

    public static final int SKOS_FORMAT = 1;
    public static final int VDEX_FORMAT = 2;

    /*public static int getVocabularyFormatFromLocation(String location) throws Exception {
        String content = (IOUtil.isURL(location))?IOUtil.readStringFromURL(new URL(location)):
                IOUtil.readStringFromFile(new File(location));
        return getVocabularyFormat(content);
    }

    public static int getVocabularyFormat(String vocContent) throws Exception {
        String rootname = null;
        Hashtable namespaces = null;
        try {
            rootname = XMLUtil.getRootTagName(vocContent);
            namespaces = XMLUtil.getAttributes(vocContent, "/");
        } catch (Exception e) {
            return -1;
        }
        String[] array = StringUtil.split(rootname, ':');
        rootname = array[array.length - 1].toLowerCase();
        if ("rdf".equals(rootname) && namespaces.containsValue(SKOS_NAMESPACE))
            return SKOS_FORMAT;
        else if ("vdex".equals(rootname) && namespaces.containsValue(VDEX_NAMESPACE))
            return VDEX_FORMAT;
        else return -1;
    }*/


}
