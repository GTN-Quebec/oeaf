package ca.licef.proeaf.metadata.util;

import ca.licef.proeaf.core.util.Constants;
import licef.tsapi.model.Triple;

public class XSLTUtil {

    public static String buildTitleTriple(String loURI, String title, String language) throws Exception {
        String lang = ("".equals(language) ? null : language);
        Triple tripleTitle = new Triple(loURI, Constants.MLR_TITLE, title, true, lang);
        return (getTripleAsString(tripleTitle));
    }

    public static String buildDescriptionTriple(String loURI, String desc, String language) throws Exception {
        String lang = ("".equals(language) ? null : language);
        Triple tripleDesc = new Triple(loURI, Constants.OEAF_DESCRIPTION, desc, true, lang);
        return (getTripleAsString(tripleDesc));
    }


    public static String getTriplesAsString(Triple[] triples) throws Exception {
        StringBuilder str = new StringBuilder();
        String tripleDelimiter = "";
        for (Triple triple : triples) {
            str.append(tripleDelimiter);
            str.append(convertTripleToString(triple));
            tripleDelimiter = "@@@";
        }
        return (str.toString());
    }

    public static String getTripleAsString(Triple triple) throws Exception {
        StringBuilder str = new StringBuilder();
        str.append(convertTripleToString(triple));
        return (str.toString());
    }

    private static String convertTripleToString(Triple triple) {
        StringBuilder str = new StringBuilder();
        str.append(triple.getSubject()).append("###");
        str.append(triple.getPredicate()).append("###");
        str.append(triple.getObject()).append("###");
        str.append(triple.isObjectLiteral());
        if (triple.getLanguage() != null)
            str.append("###").append(triple.getLanguage());
        return (str.toString());
    }

}
