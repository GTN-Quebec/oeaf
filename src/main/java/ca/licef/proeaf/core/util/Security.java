package ca.licef.proeaf.core.util;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-04-10
 */
public class Security {

    public static boolean isAuthorized(String ip) {
        /*String url = Core.getInstance().getSecurityUrl() + "/rest/isAuthorized?ip=" + ip;
        WebResource webResource = Core.getInstance().getRestClient().resource(url);
        ClientResponse response = webResource.get(ClientResponse.class);
        if( response.getStatus() == 200 ) {
            String res = response.getEntity( String.class );
            return Boolean.parseBoolean(res);
        }*/
        return false;
    }
}
