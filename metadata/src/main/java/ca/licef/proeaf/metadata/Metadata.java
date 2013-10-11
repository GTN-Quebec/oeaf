package ca.licef.proeaf.metadata;

public class Metadata {

    private static Metadata instance;

    public static Metadata getInstance() {
        if (instance == null)
            instance = new Metadata();
        return (instance);
    }



}
