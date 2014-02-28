package ca.licef.proeaf.queryengine;

import java.util.Collection;

public class ResultEntry {

    public ResultEntry() {
    }

    public ResultEntry( String id, String title, String logo ) {
        this.id = id;
        this.title = title;
        this.logo = logo;
    }

    public String getId() {
        return( id );
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getTitle() {
        return( title );
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getDescription() {
        return( description );
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getOppType() {
        return oppType;
    }

    public void setOppType(String oppType) {
        this.oppType = oppType;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    private String id;
    private String title;
    private String description;
    private String oppType;
    private String logo;

}

