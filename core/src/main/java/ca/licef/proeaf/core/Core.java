package ca.licef.proeaf.core;

import com.sun.jersey.api.client.Client;

import java.util.ResourceBundle;


/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 8-Sep-2011
 */
public class Core {
    public static java.util.Vector traces = new java.util.Vector();

    private static Core core;

    private TripleStoreService tripleStore;

    private ResourceView defaultView;

    private Client restClient;
    private String securityUrl;
    private String identityUrl;
    private String metadataUrl;
    private String vocabularyUrl;
    private String portalUrl;

    private String mulgaraUrl;

    private String proeafHome;
    private String repositoryName;
    private String adminEmail;
    private String version;
    private String repositoryNamespace;
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
            mulgaraUrl = resBundle.getString("mulgara.url");
            securityUrl = resBundle.getString("security.url");
            identityUrl = resBundle.getString("identity.url");
            metadataUrl = resBundle.getString("metadata.url");
            vocabularyUrl = resBundle.getString("vocabulary.url");
            portalUrl = resBundle.getString("portal.url");
            repositoryName = resBundle.getString("proeaf.repositoryName");
            adminEmail = resBundle.getString("proeaf.admin.email");
            version = resBundle.getString("proeaf.version");
            repositoryNamespace = resBundle.getString("proeaf.repositoryNamespace");
            smtpHost = resBundle.getString("smtp.host");

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

    public String getRepositoryName() {
        return( repositoryName );
    }

    public String getAdminEmail() {
        return( adminEmail );
    }

    public String getVersion() {
        return( version );
    }

    public String getRepositoryNamespace() {
        return( repositoryNamespace );
    }

    public String getUriPrefix() {
        return( uriPrefix );
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    /*
     * Modules endpoints
     */

    public String getSecurityUrl() {
        return securityUrl;
    }

    public String getIdentityUrl() {
        return identityUrl;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public String getVocabularyUrl() {
        return vocabularyUrl;
    }

    public String getPortalUrl() {
        return portalUrl;
    }

    /*
     * Mulgara services
     */

    public TripleStoreService getTripleStoreService() {
        if (tripleStore == null) {
            tripleStore = new TripleStoreService();
            tripleStore.setUrl(mulgaraUrl);
        }
        return tripleStore;
    }

    /*
     * Client for REST communication between modules
     */
    public Client getRestClient() {
        if (restClient == null)
            restClient = Client.create();
        return restClient;
    }

    /*
     * Default implementation of ResourceView
     */
    public ResourceView getDefaultView() {
        if (defaultView == null)
            defaultView = new DefaultView();
        return defaultView;
    }
}
