package ca.licef.proeaf.core;

import ca.licef.proeaf.vocabularies.COMETE;
import licef.tsapi.TripleStore;

import java.util.ResourceBundle;


/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 8-Sep-2011
 */
public class Core {
    private static Core core;

    private TripleStore tripleStore;

    private String proeafHome;
    private String adminEmail;
    private String version;
    private String uriPrefix;
    private String smtpHost;
    private int facetCount;

    public static Core getInstance() {
        if (core == null)
            core = new Core();
        return core;
    }

    private Core() {
        try {
            ResourceBundle resBundle = ResourceBundle.getBundle("core");
            proeafHome = resBundle.getString("proeaf.home");
            uriPrefix = resBundle.getString("proeaf.uriPrefix");
            adminEmail = resBundle.getString("proeaf.admin.email");
            version = resBundle.getString("proeaf.version");
            smtpHost = resBundle.getString("smtp.host");
            facetCount = Integer.parseInt(resBundle.getString("facets.count"));

            initTripleStore();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        try {
            ResourceBundle resBundle = ResourceBundle.getBundle("core");
            return resBundle.getString(key);
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Repository info
     */

    public String getProeafHome() {
        return proeafHome;
    }

    public String getAdminEmail() {
        return( adminEmail );
    }

    public String getVersion() {
        return( version );
    }

    public String getUriPrefix() {
        return( uriPrefix );
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getFacetCount() {
        return facetCount;
    }

    /*
     * Triple Store services
     */
    private void initTripleStore() {
        if (tripleStore == null) {
            tripleStore = new TripleStore(proeafHome + "/database", proeafHome, getUriPrefix());
            tripleStore.registerVocabulary("http://comete.licef.ca/reference#", COMETE.class);
            tripleStore.startServer(false);
        }
    }

    public TripleStore getTripleStore() {
        if (tripleStore == null)
            initTripleStore();
        return tripleStore;
    }
}
