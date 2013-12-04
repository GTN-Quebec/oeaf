package ca.licef.proeaf.queryengine;

public class ResultEntry {

    public ResultEntry() {
    }

    public ResultEntry( String id, String title, String location ) {
        this.id = id;
        this.title = title;
        this.location = location;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String id;
    private String title;
    private String description;
    private String location;

}

