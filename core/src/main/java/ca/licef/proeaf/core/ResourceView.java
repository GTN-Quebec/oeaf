package ca.licef.proeaf.core;

import ca.licef.proeaf.core.util.Triple;

import javax.servlet.ServletContext;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 28-Jun-2012
 */
public interface ResourceView {

    String getRdf(String uri, String includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable ) throws Exception;
    String getIncomingLinks( String uri, boolean isHumanReadable, int offset, int limit, String format ) throws Exception;
    String getHtml(String uri, Locale locale, ServletContext context) throws Exception;
    Triple[] getTriples(String uri, String includeIncomingLinks, boolean includeRdfMetadataInfos, boolean isHumanReadable ) throws Exception;
    
}
