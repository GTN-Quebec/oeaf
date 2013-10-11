package ca.licef.proeaf.core.util;

import ca.licef.proeaf.core.Core;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class XSLTUtil {

    public static String getImageDimension( String imgLocation ) throws Exception {
        BufferedImage image = null;
        try {
            image = ImageIO.read( new URL( imgLocation ) );
        }
        catch( Exception e ) {
            // If an error occurs, we consider the image as null. - FB
        }
        return( image == null ? "-1,-1" : image.getWidth() + "," + image.getHeight() );
    }

    public static String getIdentityUrl() {
        return( Core.getInstance().getIdentityUrl() );
    }

    public static String getMetadataUrl() {
        return( Core.getInstance().getMetadataUrl() );
    }

    public static String getVocabularyUrl() {
        return( Core.getInstance().getVocabularyUrl() );
    }

    public static String getPortalUrl() {
        return( Core.getInstance().getPortalUrl() );
    }

}
