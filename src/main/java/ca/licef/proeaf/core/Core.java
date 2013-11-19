package ca.licef.proeaf.core;

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

            initTripleStore();


        } catch (Exception e) {
            e.printStackTrace();
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


    /*
     * Triple Store services
     */
    private void initTripleStore() {
        if (tripleStore == null) {
            tripleStore = new TripleStore(proeafHome + "/databases/DB1");
            tripleStore.start();
        }
    }

    public TripleStore getTripleStore() {
        if (tripleStore == null)
            initTripleStore();
        return tripleStore;
    }


    /*
     * Client for REST communication between modules
     */
    /*public Client getRestClient() {
        if (restClient == null)
            restClient = Client.create();
        return restClient;
    }*/

}
