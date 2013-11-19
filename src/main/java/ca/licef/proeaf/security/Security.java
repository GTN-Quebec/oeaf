package ca.licef.proeaf.security;


import licef.IOUtil;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-04-10
 */
public class Security {

    private static Security instance;

    public static Security getInstance() {
        if (instance == null)
            instance = new Security();
        return (instance);
    }

    public boolean isAuthorized(String ip) throws Exception {
        //trust local address
        if (ip.equals(InetAddress.getLocalHost().getHostAddress()) ||
            "127.0.0.1".equals(ip))
            return true;

        //check on the trusted list
        Vector v;
        try {
            v = IOUtil.readLines(this.getClass().getResourceAsStream("/authorized.ini"));
        } catch (Exception e) {
            return false;
        }
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            String line = ((String)e.nextElement()).trim();
            if (line.startsWith("#") || "".equals(line))
                continue;
            if (line.equals(ip))
                return true;
        }
        return false;
    }
}
