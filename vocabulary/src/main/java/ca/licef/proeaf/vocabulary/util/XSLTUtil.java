package ca.licef.proeaf.vocabulary.util;

import ca.licef.proeaf.vocabulary.Vocabulary;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-04-24
 */

public class XSLTUtil {

    static Hashtable vocUris;
    static ArrayList rejectedIdentifiers;

    public static void initVocUris() {
        vocUris = new Hashtable();
        rejectedIdentifiers = new ArrayList();
    }

    public static String getVocabularyConceptUri(String identifier, String term) throws Exception {
        if (rejectedIdentifiers.contains(identifier))
            return null;
        if (!vocUris.containsKey(identifier)) {
            String vocUri = Vocabulary.getInstance().getVocabularyUri(identifier);
            if (vocUri != null)
                vocUris.put(identifier, vocUri);
            else {
                rejectedIdentifiers.add(identifier);
                return null;
            }
        }

        return vocUris.get(identifier) + "/" + term;
    }

}
